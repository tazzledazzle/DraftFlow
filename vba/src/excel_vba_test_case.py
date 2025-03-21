import unittest
import xlwings as xw
import os
from excel_vba_testing import ExcelVBATestSetup

class ExcelVBATestCase(unittest.TestCase):
    """Base class for Excel VBA tests"""

    excel_file_path = None  # Override this in subclasses

    def setUp(self):
        """Set up test environment before each test"""
        if not self.excel_file_path:
            self.skipTest("excel_file_path not set")

        self.excel = ExcelVBATestSetup(self.excel_file_path)
        self.wb = self.excel.setup()

    def tearDown(self):
        """Clean up after each test"""
        if hasattr(self, 'excel'):
            self.excel.teardown()

    def run_macro(self, macro_name, *args):
        """Run a VBA macro and return the result"""
        try:
            # Use xlwings to call the macro
            result = self.wb.app.macro(macro_name)(*args)
            return result
        except Exception as e:
            self.fail(f"Error running macro '{macro_name}': {str(e)}")

    def get_cell_value(self, sheet_name, cell_address):
        """Get value from a specific cell"""
        sheet = self.wb.sheets[sheet_name]
        return sheet.range(cell_address).value

    def set_cell_value(self, sheet_name, cell_address, value):
        """Set value in a specific cell"""
        sheet = self.wb.sheets[sheet_name]
        sheet.range(cell_address).value = value

    def assert_cell_value(self, sheet_name, cell_address, expected_value, msg=None):
        """Assert that a cell contains the expected value"""
        actual_value = self.get_cell_value(sheet_name, cell_address)
        self.assertEqual(actual_value, expected_value,
                         msg or f"Cell {sheet_name}!{cell_address} value doesn't match expected value")

    def assert_cell_value_approx(self, sheet_name, cell_address, expected_value,
                                 places=7, msg=None):
        """Assert that a numeric cell value is approximately equal to expected value"""
        actual_value = self.get_cell_value(sheet_name, cell_address)
        self.assertAlmostEqual(actual_value, expected_value, places=places,
                               msg=msg or f"Cell {sheet_name}!{cell_address} value not approximately equal")

    def assert_range_values(self, sheet_name, range_address, expected_values, msg=None):
        """Assert that a range of cells contains the expected values"""
        sheet = self.wb.sheets[sheet_name]
        actual_values = sheet.range(range_address).value

        # Handle single cell case
        if not isinstance(actual_values, list):
            actual_values = [[actual_values]]
        elif not isinstance(actual_values[0], list):
            actual_values = [actual_values]

        # Handle single cell expected value
        if not isinstance(expected_values, list):
            expected_values = [[expected_values]]
        elif not isinstance(expected_values[0], list):
            expected_values = [expected_values]

        # Compare row by row
        for i, (actual_row, expected_row) in enumerate(zip(actual_values, expected_values)):
            for j, (actual, expected) in enumerate(zip(actual_row, expected_row)):
                self.assertEqual(actual, expected,
                                 msg or f"Value mismatch at row {i+1}, column {j+1} in range {sheet_name}!{range_address}")