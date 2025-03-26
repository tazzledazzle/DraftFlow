import os
import time
import logging
import pandas as pd
from typing import Any, Dict, List, Optional, Union
import xlwings as xw

from vba_test_framework import VBATestException

class XlwingsExcelApplication:
    """Excel application wrapper using xlwings"""
    
    def __init__(self, visible: bool = False):
        """
        Initialize Excel application using xlwings
        
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
            logging.info("Excel application started with xlwings")
        except Exception as e:
            logging.error(f"Failed to start Excel with xlwings: {e}")
            raise VBATestException(f"Failed to start Excel with xlwings: {e}")
    
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
    
    def set_range_from_dataframe(self, sheet_name: str, start_cell: str, df: pd.DataFrame, index: bool = False, header: bool = True):
        """
        Set range values from a pandas DataFrame
        
        Args:
            sheet_name: Name of the worksheet
            start_cell: Top-left cell address (e.g., "A1")
            df: DataFrame to write
            index: Whether to include DataFrame index
            header: Whether to include DataFrame header
        """
        try:
            self.app.books.active.sheets[sheet_name].range(start_cell).options(index=index, header=header).value = df
            logging.info(f"Set values in range starting at {sheet_name}!{start_cell} from DataFrame")
        except Exception as e:
            logging.error(f"Failed to set range from DataFrame at {sheet_name}!{start_cell}: {e}")
            raise VBATestException(f"Failed to set range from DataFrame: {e}")
    
    def get_cell_formula(self, sheet_name: str, row: int, col: int) -> str:
        """
        Get formula from a cell
        
        Args:
            sheet_name: Name of the worksheet
            row: Row number (1-based)
            col: Column number (1-based)
            
        Returns:
            Cell formula as string
        """
        try:
            return self.app.books.active.sheets[sheet_name].cells(row, col).formula
        except Exception as e:
            logging.error(f"Failed to get cell formula at {sheet_name}!({row},{col}): {e}")
            raise VBATestException(f"Failed to get cell formula: {e}")
    
    def get_cell_number_format(self, sheet_name: str, row: int, col: int) -> str:
        """
        Get number format from a cell
        
        Args:
            sheet_name: Name of the worksheet
            row: Row number (1-based)
            col: Column number (1-based)
            
        Returns:
            Cell number format as string
        """
        try:
            return self.app.books.active.sheets[sheet_name].cells(row, col).number_format
        except Exception as e:
            logging.error(f"Failed to get cell number format at {sheet_name}!({row},{col}): {e}")
            raise VBATestException(f"Failed to get cell number format: {e}")