# Install required packages
# pip install xlwings pytest

import os
import unittest
import xlwings as xw
import time
import sys

class ExcelVBATestSetup:
    """Helper class to set up and tear down Excel for testing"""

    def __init__(self, excel_file_path):
        self.excel_file_path = excel_file_path
        self.app = None
        self.wb = None

    def setup(self):
        """Start Excel and open the workbook"""
        # Start Excel application
        self.app = xw.App(visible=False)  # Set to True for debugging
        # Disable alerts (like file already open warnings)
        self.app.display_alerts = False

        # Check if file exists
        if not os.path.exists(self.excel_file_path):
            raise FileNotFoundError(f"Excel file not found: {self.excel_file_path}")

        # Open the workbook
        self.wb = self.app.books.open(self.excel_file_path)
        return self.wb

    def teardown(self):
        """Close Excel and clean up"""
        if self.wb:
            # Save any changes if needed
            # self.wb.save()  # Uncomment if you want to save changes
            self.wb.close()
            self.wb = None

        if self.app:
            self.app.quit()
            self.app = None