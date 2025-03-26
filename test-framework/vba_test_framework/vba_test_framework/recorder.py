import xlwings as xw
import time
import json
import os
from typing import Dict, List, Any, Optional
import logging

class VBARecorder:
    """Records VBA macro executions to generate test cases"""

    def __init__(self, excel_path: str, visible: bool = True):
        """
        Initialize VBA recorder

        Args:
            excel_path: Path to Excel file
            visible: Whether Excel should be visible
        """
        self.excel_path = os.path.abspath(excel_path)
        self.visible = visible
        self.app = None
        self.workbook = None
        self.events = []
        self.start_state = {}
        self.end_state = {}

    def start(self):
        """Start recording session"""
        try:
            # Start Excel
            self.app = xw.App(visible=self.visible, add_book=False)
            self.app.display_alerts = False

            # Open workbook
            self.workbook = self.app.books.open(self.excel_path)

            # Clear previous recording
            self.events = []
            self.start_state = {}
            self.end_state = {}

            logging.info(f"Recording started for {self.excel_path}")
            return True
        except Exception as e:
            logging.error(f"Failed to start recording: {e}")
            self.stop()
            return False

    def stop(self):
        """Stop recording session and clean up"""
        try:
            if self.workbook:
                self.workbook.close()
                self.workbook = None

            if self.app:
                self.app.quit()
                self.app = None

            logging.info("Recording stopped")
        except Exception as e:
            logging.error(f"Error stopping recording: {e}")

    def capture_sheet_state(self, sheet_name: str, range_address: str) -> Dict[str, Any]:
        """
        Capture state of a worksheet range

        Args:
            sheet_name: Name of worksheet
            range_address: Range to capture (e.g., "A1:D10")

        Returns:
            Dictionary with sheet state
        """
        try:
            sheet = self.workbook.sheets[sheet_name]
            range_obj = sheet.range(range_address)

            # Get values
            values = []
            rows, cols = range_obj.shape
            for row in range(rows):
                row_values = []
                for col in range(cols):
                    cell = range_obj[row, col]
                    row_values.append({
                        "value": cell.value,
                        "formula": cell.formula if cell.formula else None,
                        "address": cell.address
                    })
                values.append(row_values)

            return {
                "sheet_name": sheet_name,
                "range_address": range_address,
                "values": values
            }
        except Exception as e:
            logging.error(f"Failed to capture sheet state: {e}")
            return {}

    def record_macro(self, macro_name: str, args: List[Any] = None,
                     input_ranges: List[Dict[str, str]] = None,
                     output_ranges: List[Dict[str, str]] = None):
        """
        Record execution of a VBA macro

        Args:
            macro_name: Name of the macro to run
            args: Arguments to pass to the macro
            input_ranges: List of input ranges to capture before execution
                (each a dict with 'sheet' and 'range' keys)
            output_ranges: List of output ranges to capture after execution
                (each a dict with 'sheet' and 'range' keys)
        """
        if not self.app:
            logging.error("Excel application not started. Call start() first.")
            return False

        try:
            event = {
                "type": "macro",
                "name": macro_name,
                "args": args or [],
                "timestamp": time.time(),
                "inputs": {},
                "outputs": {},
                "result": None
            }

            # Capture input state
            if input_ranges:
                for input_def in input_ranges:
                    sheet = input_def.get("sheet", "Sheet1")
                    range_addr = input_def.get("range", "A1")
                    key = f"{sheet}!{range_addr}"
                    event["inputs"][key] = self.capture_sheet_state(sheet, range_addr)

            # Run the macro and capture result
            result = None
            if args:
                result = self.workbook.macro(macro_name)(*args)
            else:
                result = self.workbook.macro(macro_name)()

            event["result"] = result

            # Capture output state
            if output_ranges:
                for output_def in output_ranges:
                    sheet = output_def.get("sheet", "Sheet1")
                    range_addr = output_def.get("range", "A1")
                    key = f"{sheet}!{range_addr}"
                    event["outputs"][key] = self.capture_sheet_state(sheet, range_addr)

            self.events.append(event)
            logging.info(f"Recorded macro execution: {macro_name}")
            return True
        except Exception as e:
            logging.error(f"Failed to record macro {macro_name}: {e}")
            return False

    def record_cell_update(self, sheet_name: str, row: int, col: int, value: Any):
        """
        Record a cell update

        Args:
            sheet_name: Name of worksheet
            row: Row number (1-based)
            col: Column number (1-based)
            value: Value to set
        """
        if not self.app:
            logging.error("Excel application not started. Call start() first.")
            return False

        try:
            # Get cell address
            cell = self.workbook.sheets[sheet_name].cells(row, col)
            address = cell.address

            # Capture previous value
            prev_value = cell.value

            # Record event
            event = {
                "type": "cell_update",
                "sheet": sheet_name,
                "address": address,
                "row": row,
                "col": col,
                "prev_value": prev_value,
                "new_value": value,
                "timestamp": time.time()
            }

            # Update cell
            cell.value = value

            self.events.append(event)
            logging.info(f"Recorded cell update: {sheet_name}!{address} = {value}")
            return True
        except Exception as e:
            logging.error(f"Failed to record cell update: {e}")
            return False

    def record_range_update(self, sheet_name: str, range_address: str, values: List[List[Any]]):
        """
        Record a range update

        Args:
            sheet_name: Name of worksheet
            range_address: Range address (e.g., "A1:C3")
            values: 2D list of values to set
        """
        if not self.app:
            logging.error("Excel application not started. Call start() first.")
            return False

        try:
            # Get range
            range_obj = self.workbook.sheets[sheet_name].range(range_address)

            # Capture previous values
            prev_values = range_obj.value

            # Ensure prev_values is a 2D list
            if not isinstance(prev_values, list):
                prev_values = [[prev_values]]
            elif prev_values and not isinstance(prev_values[0], list):
                prev_values = [prev_values]

            # Record event
            event = {
                "type": "range_update",
                "sheet": sheet_name,
                "range": range_address,
                "prev_values": prev_values,
                "new_values": values,
                "timestamp": time.time()
            }

            # Update range
            range_obj.value = values

            self.events.append(event)
            logging.info(f"Recorded range update: {sheet_name}!{range_address}")
            return True
        except Exception as e:
            logging.error(f"Failed to record range update: {e}")
            return False

    def generate_test_class(self, class_name: str, output_file: str = None) -> str:
        """
        Generate a test class from recorded events
        
        Args:
            class_name: Name for the test class
            output_file: Optional file path to save the generated code
            
        Returns:
            Generated Python code as string
        """
        if not self.events:
            logging.warning("No events recorded. Cannot generate test class.")
            return ""

        # Create template
        code = f"""import os
import sys
from datetime import datetime
from typing import Dict, List, Any

# Add parent directory to path for imports
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from vba_test_framework import VBATestCase, vba_test, ExcelApplication, create_test_suite_from_class
from vba_assertions import VBAAssertions

class {class_name}(VBATestCase):
    \"\"\"Test case generated from recording on {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\"\"\"
    
    def setup(self):
        \"\"\"Set up the test environment\"\"\"
        # Path to the Excel file (update with your file path)
        excel_path = r"{self.excel_path}"
        super().setup(excel_path)
    
"""

        # Generate test methods
        for i, event in enumerate(self.events):
            if event["type"] == "macro":
                method_name = f"test_{i+1:02d}_{event['name']}"

                # Create test method
                code += f"    @vba_test\n"
                code += f"    def {method_name}(self):\n"
                code += f"        \"\"\"Test {event['name']} macro\"\"\"\n"

                # Set up input values if any
                for key, input_data in event["inputs"].items():
                    sheet, range_addr = key.split("!", 1)
                    input_values = input_data.get("values", [])

                    if len(input_values) == 1 and len(input_values[0]) == 1:
                        # Single cell
                        cell_value = input_values[0][0]["value"]
                        cell_addr = input_values[0][0]["address"].replace("$", "")
                        row, col = self._address_to_row_col(cell_addr)

                        code += f"        # Set input value in {key}\n"
                        code += f"        self.excel.set_cell_value(\"{sheet}\", {row}, {col}, {repr(cell_value)})\n"
                    else:
                        # Range of cells
                        flat_values = []
                        for row_values in input_values:
                            flat_row = []
                            for cell in row_values:
                                flat_row.append(cell["value"])
                            flat_values.append(flat_row)

                        code += f"        # Set input values in {key}\n"
                        code += f"        input_values = {repr(flat_values)}\n"
                        code += f"        self.excel.set_range_values(\"{sheet}\", \"{range_addr}\", input_values)\n"

                # Run the macro
                args_str = ", ".join(repr(arg) for arg in event["args"])
                code += f"\n        # Run the macro\n"
                if event["result"] is not None:
                    code += f"        result = self.excel.run_macro(\"{event['name']}\", {args_str})\n"
                    code += f"        # Verify result\n"
                    code += f"        VBAAssertions.assert_equal(result, {repr(event['result'])})\n"
                else:
                    code += f"        self.excel.run_macro(\"{event['name']}\", {args_str})\n"

                # Verify output values if any
                for key, output_data in event["outputs"].items():
                    sheet, range_addr = key.split("!", 1)
                    output_values = output_data.get("values", [])

                    if len(output_values) == 1 and len(output_values[0]) == 1:
                        # Single cell
                        cell_value = output_values[0][0]["value"]
                        cell_addr = output_values[0][0]["address"].replace("$", "")
                        row, col = self._address_to_row_col(cell_addr)

                        code += f"\n        # Verify output value in {key}\n"
                        code += f"        VBAAssertions.assert_cell_value(self.excel, \"{sheet}\", {row}, {col}, {repr(cell_value)})\n"
                    else:
                        # Range of cells
                        flat_values = []
                        for row_values in output_values:
                            flat_row = []
                            for cell in row_values:
                                flat_row.append(cell["value"])
                            flat_values.append(flat_row)

                        code += f"\n        # Verify output values in {key}\n"
                        code += f"        expected_values = {repr(flat_values)}\n"
                        code += f"        actual_values = self.excel.get_range_values(\"{sheet}\", \"{range_addr}\")\n"
                        code += f"        VBAAssertions.assert_equal(actual_values, expected_values)\n"

            elif event["type"] == "cell_update":
                method_name = f"test_{i+1:02d}_cell_update_{event['sheet']}_{event['row']}_{event['col']}"

                code += f"    @vba_test\n"
                code += f"    def {method_name}(self):\n"
                code += f"        \"\"\"Test cell update in {event['sheet']}!{event['address']}\"\"\"\n"
                code += f"        # Set cell value\n"
                code += f"        self.excel.set_cell_value(\"{event['sheet']}\", {event['row']}, {event['col']}, {repr(event['new_value'])})\n"
                code += f"        # Verify cell value\n"
                code += f"        VBAAssertions.assert_cell_value(self.excel, \"{event['sheet']}\", {event['row']}, {event['col']}, {repr(event['new_value'])})\n"

            elif event["type"] == "range_update":
                method_name = f"test_{i+1:02d}_range_update_{event['sheet']}_{event['range'].replace(':', '_')}"

                code += f"    @vba_test\n"
                code += f"    def {method_name}(self):\n"
                code += f"        \"\"\"Test range update in {event['sheet']}!{event['range']}\"\"\"\n"
                code += f"        # Set range values\n"
                code += f"        values = {repr(event['new_values'])}\n"
                code += f"        self.excel.set_range_values(\"{event['sheet']}\", \"{event['range']}\", values)\n"
                code += f"        # Verify range values\n"
                code += f"        actual_values = self.excel.get_range_values(\"{event['sheet']}\", \"{event['range']}\")\n"
                code += f"        VBAAssertions.assert_equal(actual_values, values)\n"

            code += "\n"

        # Add main section to run tests
        code += f"""
if __name__ == "__main__":
    # Create and run test suite
    test_suite = create_test_suite_from_class({class_name}, "{class_name}", r"{self.excel_path}", visible=True)
    results = test_suite.run()
    
    # Generate report
    report_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "{class_name}_report.html")
    test_suite.generate_report(results, report_path)
    
    # Print summary
    print(f"Tests: {{results['total']}}, Passed: {{results['passed']}}, Failed: {{results['failed']}}, Errors: {{results['errors']}}")
    print(f"Report generated at {{report_path}}")
"""

        # Write to file if specified
        if output_file:
            with open(output_file, 'w') as f:
                f.write(code)
            logging.info(f"Test class generated and saved to {output_file}")

        return code

    def save_recording(self, file_path: str):
        """
        Save recording to file
        
        Args:
            file_path: Path to save recording
        """
        data = {
            "file": self.excel_path,
            "timestamp": time.time(),
            "events": self.events
        }

        with open(file_path, 'w') as f:
            json.dump(data, f, indent=2)

        logging.info(f"Recording saved to {file_path}")

    def load_recording(self, file_path: str):
        """
        Load recording from file
        
        Args:
            file_path: Path to load recording from
        """
        with open(file_path, 'r') as f:
            data = json.load(f)

        self.excel_path = data.get("file", self.excel_path)
        self.events = data.get("events", [])

        logging.info(f"Recording loaded from {file_path} with {len(self.events)} events")

    def _address_to_row_col(self, address: str) -> tuple:
        """
        Convert Excel address to row and column numbers
        
        Args:
            address: Cell address (e.g., "A1")
            
        Returns:
            Tuple of (row, column) numbers (1-based)
        """
        # Extract column letters and row number
        col_str = ""
        row_str = ""

        for char in address:
            if char.isalpha():
                col_str += char
            else:
                row_str += char

        # Convert column letters to number
        col_num = 0
        for char in col_str:
            col_num = col_num * 26 + (ord(char.upper()) - ord('A') + 1)

        row_num = int(row_str)

        return row_num, col_num