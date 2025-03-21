import unittest
import os
from excel_vba_test_case import ExcelVBATestCase


class MyExcelMacroTests(ExcelVBATestCase):
    """Unit tests for macros in a specific Excel workbook"""

    # Update this path to point to your Excel file
    excel_file_path = r"../tests/Test6446Job.xlsx"

    def test_calculation_macro(self):
        """Test a macro that performs calculations"""
        # Set up test data
        self.set_cell_value("Sheet1", "AA1", 10)
        self.set_cell_value("Sheet1", "AA2", 20)

        # Run the macro
        self.run_macro("CalculateSum")

        # Check the result
        self.assert_cell_value("Sheet1", "AA3", 30)

    def test_data_transformation_macro(self):
        """Test a macro that transforms data"""
        # Set up test data
        test_data = [["Name", "Score"], ["Alice", 85], ["Bob", 92], ["Charlie", 78]]
        self.wb.sheets["Sheet2"].range("A1").value = test_data

        # Run the macro
        self.run_macro("TransformData")

        # Check that grades were correctly assigned
        expected_grades = [["Grade"], ["B"], ["A"], ["C"]]
        self.assert_range_values("Sheet2", "C1:C4", expected_grades)

    def test_macro_with_parameters(self):
        """Test a macro that takes parameters"""
        # Run the macro with parameters
        result = self.run_macro("MultiplyNumbers", 5, 7)

        # Check the result if the macro returns a value
        self.assertEqual(result, 35)

        # Alternatively, check a cell where the result might be written
        self.assert_cell_value("Sheet1", "D1", 35)

    def test_data_import_macro(self):
        """Test a macro that imports or processes data"""
        # Create a small test file if your macro reads external files
        # test_file_path = self._create_test_file()

        # Set the file path in a cell if the macro reads it from there
        self.set_cell_value("Sheet3", "A1", r"C:\path\to\test_data.csv")

        # Run the import macro
        self.run_macro("ImportData")

        # Check that data was imported correctly
        self.assertTrue(self.get_cell_value("Sheet3", "B1") is not None)
        self.assertEqual(self.get_cell_value("Sheet3", "B2"), "Expected header")

    def test_formatting_macro(self):
        """Test a macro that applies formatting"""
        # Set up test data
        self.set_cell_value("Sheet1", "E1", "Test")

        # Run the formatting macro
        self.run_macro("FormatCells")

        # Check formatting (xlwings can check some formatting properties)
        cell = self.wb.sheets["Sheet1"].range("E1")
        self.assertEqual(cell.font.bold, True)
        self.assertEqual(cell.font.color, (255, 0, 0))  # RGB for red

    def test_error_handling_macro(self):
        """Test a macro's error handling capabilities"""
        # Set up data that should trigger an error
        self.set_cell_value("Sheet1", "F1", 0)  # For division by zero

        # Run the macro that should handle this error
        self.run_macro("DivideNumbers")

        # Check that the error was handled correctly
        self.assert_cell_value("Sheet1", "F2", "Error: Division by zero")

    # Add more test methods for other macros...

    def _create_test_file(self):
        """Helper method to create a test file for data import tests"""
        test_file_path = os.path.join(
            os.path.dirname(self.excel_file_path), "test_data.csv"
        )
        with open(test_file_path, "w") as f:
            f.write("Header1,Header2,Header3\n")
            f.write("Value1,Value2,Value3\n")
        return test_file_path


if __name__ == "__main__":
    unittest.main()
