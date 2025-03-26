import logging
from typing import Literal, Union, Optional

from vba_test_framework.vba_test_framework.core import ExcelApplication, VBATestException

class ExcelBackendFactory:
    """Factory class for creating Excel application backend instances"""

    @staticmethod
    def create_excel_app(backend: Literal["xlwings"] = "xlwings", visible: bool = False) -> ExcelApplication:
        """
        Create an Excel application instance

        Args:
            backend: Parameter kept for backward compatibility but not used
                     as we now exclusively use xlwings
            visible: Whether Excel should be visible

        Returns:
            Excel application instance
        """
        return ExcelApplication(visible=visible)


class XlwingsVBATestCase:
    """Base class for VBA test cases"""

    def __init__(self, excel_app: Optional[ExcelApplication] = None):
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