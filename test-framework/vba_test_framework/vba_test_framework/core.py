import os
import time
import xlwings as xw
import pandas as pd
from pathlib import Path
import logging
from typing import Any, Dict, List, Optional, Union, Callable

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("vba_test.log"),
        logging.StreamHandler()
    ]
)

class VBATestException(Exception):
    """Custom exception for VBA testing errors"""
    pass

class ExcelApplication:
    """Class to manage Excel application instance"""

    def __init__(self, visible: bool = False):
        """
        Initialize Excel application

        Args:
            visible: Whether Excel is visible during testing
        """
        self.visible = visible
        self.app = None
        self.workbooks = []

    def start(self):
        """Start Excel application instance"""
        try:
            self.app = xw.App(visible=self.visible, add_book=False)
            self.app.display_alerts = False
            self.app.screen_updating = True  # Set to True to see changes during testing
            logging.info("Excel application started")
        except Exception as e:
            logging.error(f"Failed to start Excel: {e}")
            raise VBATestException(f"Failed to start Excel: {e}")

    def quit(self):
        """Quit Excel application and clean up"""
        try:
            if self.app:
                # Close all workbooks without saving
                for wb_name in self.workbooks:
                    if wb_name in [wb.name for wb in self.app.books]:
                        wb = self.app.books[wb_name]
                        wb.close()

                self.app.quit()
                self.app = None
                self.workbooks = []
                logging.info("Excel application closed")
        except Exception as e:
            logging.error(f"Error closing Excel: {e}")

    def open_workbook(self, file_path: str) -> str:
        """
        Open an Excel workbook

        Args:
            file_path: Path to Excel file

        Returns:
            Name of the opened workbook
        """
        try:
            abs_path = os.path.abspath(file_path)
            if not os.path.exists(abs_path):
                raise VBATestException(f"File not found: {abs_path}")

            wb = self.app.books.open(abs_path)
            self.workbooks.append(wb.name)
            logging.info(f"Opened workbook: {wb.name}")
            return wb.name
        except Exception as e:
            logging.error(f"Failed to open workbook {file_path}: {e}")
            raise VBATestException(f"Failed to open workbook: {e}")

    def run_macro(self, macro_name: str, *args) -> Any:
        """
        Run a VBA macro

        Args:
            macro_name: Name of the macro to run
            *args: Arguments to pass to the macro

        Returns:
            Result of the macro if any
        """
        try:
            # Get active workbook
            if not self.app.books:
                raise VBATestException("No workbook is open")

            wb = self.app.books.active

            # Run the macro
            result = wb.macro(macro_name)(*args)
            logging.info(f"Executed macro: {macro_name}")
            return result
        except Exception as e:
            logging.error(f"Failed to run macro {macro_name}: {e}")
            raise VBATestException(f"Failed to run macro {macro_name}: {e}")

    def get_cell_value(self, sheet_name: str, row: int, col: int) -> Any:
        """
        Get value from a cell

        Args:
            sheet_name: Name of the worksheet
            row: Row number (1-based)
            col: Column number (1-based)

        Returns:
            Cell value
        """
        try:
            return self.app.books.active.sheets[sheet_name].cells(row, col).value
        except Exception as e:
            logging.error(f"Failed to get cell value at {sheet_name}!({row},{col}): {e}")
            raise VBATestException(f"Failed to get cell value: {e}")

    def set_cell_value(self, sheet_name: str, row: int, col: int, value: Any):
        """
        Set value in a cell

        Args:
            sheet_name: Name of the worksheet
            row: Row number (1-based)
            col: Column number (1-based)
            value: Value to set
        """
        try:
            self.app.books.active.sheets[sheet_name].cells(row, col).value = value
            logging.info(f"Set cell {sheet_name}!({row},{col}) to {value}")
        except Exception as e:
            logging.error(f"Failed to set cell value at {sheet_name}!({row},{col}): {e}")
            raise VBATestException(f"Failed to set cell value: {e}")

    def get_range_values(self, sheet_name: str, range_address: str) -> List[List[Any]]:
        """
        Get values from a range

        Args:
            sheet_name: Name of the worksheet
            range_address: Range address (e.g., "A1:C5")

        Returns:
            2D list of values
        """
        try:
            values = self.app.books.active.sheets[sheet_name].range(range_address).value

            # xlwings returns a single value if the range is one cell
            if not isinstance(values, list):
                return [[values]]

            # Ensure we have a 2D list
            if values and not isinstance(values[0], list):
                values = [values]

            return values
        except Exception as e:
            logging.error(f"Failed to get range values from {sheet_name}!{range_address}: {e}")
            raise VBATestException(f"Failed to get range values: {e}")

    def set_range_values(self, sheet_name: str, range_address: str, values: List[List[Any]]):
        """
        Set values in a range

        Args:
            sheet_name: Name of the worksheet
            range_address: Range address (e.g., "A1:C5")
            values: 2D list of values to set
        """
        try:
            self.app.books.active.sheets[sheet_name].range(range_address).value = values
            logging.info(f"Set values in range {sheet_name}!{range_address}")
        except Exception as e:
            logging.error(f"Failed to set range values in {sheet_name}!{range_address}: {e}")
            raise VBATestException(f"Failed to set range values: {e}")

    def get_range_as_dataframe(self, sheet_name: str, range_address: str, header: bool = True) -> pd.DataFrame:
        """
        Get range values as a pandas DataFrame

        Args:
            sheet_name: Name of the worksheet
            range_address: Range address (e.g., "A1:C5")
            header: Whether first row contains headers

        Returns:
            DataFrame containing the range values
        """
        try:
            # Use xlwings' built-in DataFrame conversion
            df = self.app.books.active.sheets[sheet_name].range(range_address).options(pd.DataFrame, header=header).value
            return df
        except Exception as e:
            logging.error(f"Failed to get range as DataFrame from {sheet_name}!{range_address}: {e}")
            raise VBATestException(f"Failed to get range as DataFrame: {e}")

class VBATestCase:
    """Base class for VBA test cases"""

    def __init__(self, excel_app: ExcelApplication = None):
        """
        Initialize test case
        
        Args:
            excel_app: Excel application instance to use (or create new if None)
        """
        self.excel = excel_app if excel_app else ExcelApplication()
        self.workbook_name = None
        self._setup_done = False

    def setup(self, workbook_path: str):
        """
        Set up the test case
        
        Args:
            workbook_path: Path to workbook containing VBA code
        """
        if not self._setup_done:
            if not self.excel.app:
                self.excel.start()
            self.workbook_name = self.excel.open_workbook(workbook_path)
            self._setup_done = True

    def teardown(self):
        """Clean up after test case"""
        self.excel.quit()
        self._setup_done = False

    def run_test(self, test_func: Callable) -> Dict[str, Any]:
        """
        Run a test function and return results
        
        Args:
            test_func: Test function to run
            
        Returns:
            Dictionary with test results
        """
        result = {
            "name": test_func.__name__,
            "status": "PASSED",
            "error": None,
            "start_time": time.time(),
            "execution_time": 0
        }

        try:
            test_func()
        except AssertionError as e:
            result["status"] = "FAILED"
            result["error"] = str(e)
        except Exception as e:
            result["status"] = "ERROR"
            result["error"] = f"{type(e).__name__}: {str(e)}"
        finally:
            result["execution_time"] = time.time() - result["start_time"]

        return result

class VBATestSuite:
    """Test suite to run multiple VBA tests"""

    def __init__(self, name: str, workbook_path: str, visible: bool = False):
        """
        Initialize test suite
        
        Args:
            name: Name of the test suite
            workbook_path: Path to Excel workbook with VBA code
            visible: Whether Excel should be visible during tests
        """
        self.name = name
        self.workbook_path = os.path.abspath(workbook_path)
        self.excel = ExcelApplication(visible=visible)
        self.test_cases = []
        self.results = []

    def add_test_case(self, test_case: VBATestCase):
        """
        Add a test case to the suite
        
        Args:
            test_case: Test case to add
        """
        self.test_cases.append(test_case)

    def run(self) -> Dict[str, Any]:
        """
        Run all test cases in the suite
        
        Returns:
            Dictionary with test results
        """
        suite_result = {
            "name": self.name,
            "total": len(self.test_cases),
            "passed": 0,
            "failed": 0,
            "errors": 0,
            "start_time": time.time(),
            "execution_time": 0,
            "test_results": []
        }

        try:
            self.excel.start()
            self.excel.open_workbook(self.workbook_path)

            for test_case in self.test_cases:
                # Set Excel instance for test case
                test_case.excel = self.excel
                test_case._setup_done = True

                # Run the test
                result = test_case.run_test(test_case.run)
                suite_result["test_results"].append(result)

                # Update counts
                if result["status"] == "PASSED":
                    suite_result["passed"] += 1
                elif result["status"] == "FAILED":
                    suite_result["failed"] += 1
                else:
                    suite_result["errors"] += 1

                logging.info(f"Test {result['name']} {result['status']}")
                if result["error"]:
                    logging.error(f"  Error: {result['error']}")

        except Exception as e:
            logging.error(f"Error running test suite: {e}")
            raise
        finally:
            self.excel.quit()
            suite_result["execution_time"] = time.time() - suite_result["start_time"]

        return suite_result

    def generate_report(self, results: Dict[str, Any], output_file: str = None) -> str:
        """
        Generate HTML report from test results
        
        Args:
            results: Test results from run()
            output_file: Path to save HTML report
            
        Returns:
            HTML report as string
        """
        html = f"""<!DOCTYPE html>
<html>
<head>
    <title>VBA Test Report - {results['name']}</title>
    <style>
        body {{ font-family: Arial, sans-serif; margin: 20px; }}
        h1 {{ color: #333366; }}
        .summary {{ margin: 20px 0; padding: 10px; background-color: #f5f5f5; border-radius: 5px; }}
        .test-case {{ margin: 5px 0; padding: 10px; border-radius: 5px; }}
        .PASSED {{ background-color: #dff0d8; }}
        .FAILED {{ background-color: #f2dede; }}
        .ERROR {{ background-color: #fcf8e3; }}
        .details {{ margin-top: 5px; font-family: monospace; white-space: pre-wrap; }}
    </style>
</head>
<body>
    <h1>VBA Test Report - {results['name']}</h1>
    
    <div class="summary">
        <h2>Summary</h2>
        <p>Total Tests: {results['total']}</p>
        <p>Passed: {results['passed']}</p>
        <p>Failed: {results['failed']}</p>
        <p>Errors: {results['errors']}</p>
        <p>Execution Time: {results['execution_time']:.2f} seconds</p>
    </div>
    
    <h2>Test Results</h2>
"""

        for result in results['test_results']:
            html += f"""
    <div class="test-case {result['status']}">
        <h3>{result['name']} - {result['status']}</h3>
        <p>Execution Time: {result['execution_time']:.2f} seconds</p>
"""
            if result['error']:
                html += f"""
        <div class="details">
            Error: {result['error']}
        </div>
"""
            html += "    </div>\n"

        html += """
</body>
</html>
"""

        if output_file:
            with open(output_file, 'w') as f:
                f.write(html)
            logging.info(f"Test report written to {output_file}")

        return html

# Utility function to create test case decorators
def vba_test(func):
    """Decorator to mark a method as a VBA test"""
    func._vba_test = True
    return func

def create_test_suite_from_class(cls, name, workbook_path, visible=False):
    """
    Create a test suite from a test class
    
    Args:
        cls: Test class
        name: Test suite name
        workbook_path: Path to Excel workbook
        visible: Whether Excel should be visible
        
    Returns:
        VBATestSuite instance
    """
    suite = VBATestSuite(name, workbook_path, visible)

    # Find all test methods
    for method_name in dir(cls):
        method = getattr(cls, method_name)
        if callable(method) and hasattr(method, '_vba_test') and method._vba_test:
            # Create an instance for each test method
            instance = cls()
            instance.run = method.__get__(instance, cls)
            suite.add_test_case(instance)

    return suite