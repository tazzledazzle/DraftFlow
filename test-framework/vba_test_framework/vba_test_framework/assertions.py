import pandas as pd
import numpy as np
from typing import Any, List, Dict, Union
import logging

class VBAAssertions:
    """Assertion methods for VBA testing"""
    
    @staticmethod
    def assert_equal(actual: Any, expected: Any, msg: str = None):
        """
        Assert that actual equals expected
        
        Args:
            actual: Actual value
            expected: Expected value
            msg: Optional message
        
        Raises:
            AssertionError: If assertion fails
        """
        if actual != expected:
            error_msg = f"Expected {expected}, got {actual}"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_not_equal(actual: Any, expected: Any, msg: str = None):
        """
        Assert that actual does not equal expected
        
        Args:
            actual: Actual value
            expected: Expected value
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        if actual == expected:
            error_msg = f"Expected {actual} to be different from {expected}"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_almost_equal(actual: float, expected: float, precision: int = 7, msg: str = None):
        """
        Assert that actual equals expected within precision
        
        Args:
            actual: Actual value
            expected: Expected value
            precision: Number of decimal places to check
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        if round(abs(actual - expected), precision) != 0:
            error_msg = f"Expected {expected}, got {actual} (precision: {precision})"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_true(condition: bool, msg: str = None):
        """
        Assert that condition is True
        
        Args:
            condition: Condition to check
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        if not condition:
            error_msg = "Expected True, got False"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_false(condition: bool, msg: str = None):
        """
        Assert that condition is False
        
        Args:
            condition: Condition to check
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        if condition:
            error_msg = "Expected False, got True"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_none(value: Any, msg: str = None):
        """
        Assert that value is None
        
        Args:
            value: Value to check
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        if value is not None:
            error_msg = f"Expected None, got {value}"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_not_none(value: Any, msg: str = None):
        """
        Assert that value is not None
        
        Args:
            value: Value to check
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        if value is None:
            error_msg = "Expected not None, got None"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_in(member: Any, container: Any, msg: str = None):
        """
        Assert that member is in container
        
        Args:
            member: Member to check
            container: Container to check
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        if member not in container:
            error_msg = f"Expected {member} to be in {container}"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_not_in(member: Any, container: Any, msg: str = None):
        """
        Assert that member is not in container
        
        Args:
            member: Member to check
            container: Container to check
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        if member in container:
            error_msg = f"Expected {member} to not be in {container}"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_cell_value(excel_app, sheet_name: str, row: int, col: int, 
                         expected: Any, msg: str = None):
        """
        Assert that cell has expected value
        
        Args:
            excel_app: Excel application instance
            sheet_name: Sheet name
            row: Row number (1-based)
            col: Column number (1-based)
            expected: Expected value
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        actual = excel_app.get_cell_value(sheet_name, row, col)
        if actual != expected:
            cell_ref = f"{sheet_name}!{chr(64 + col)}{row}"
            error_msg = f"Expected {cell_ref} to be {expected}, got {actual}"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_cell_formula(excel_app, sheet_name: str, row: int, col: int, 
                           expected: str, msg: str = None):
        """
        Assert that cell has expected formula
        
        Args:
            excel_app: Excel application instance
            sheet_name: Sheet name
            row: Row number (1-based)
            col: Column number (1-based)
            expected: Expected formula
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        try:
            actual = excel_app.app.Worksheets(sheet_name).Cells(row, col).Formula
            if actual != expected:
                cell_ref = f"{sheet_name}!{chr(64 + col)}{row}"
                error_msg = f"Expected formula in {cell_ref} to be {expected}, got {actual}"
                if msg:
                    error_msg = f"{msg}: {error_msg}"
                raise AssertionError(error_msg)
        except Exception as e:
            logging.error(f"Failed to get cell formula: {e}")
            raise AssertionError(f"Failed to get cell formula: {e}")
    
    @staticmethod
    def assert_range_values(excel_app, sheet_name: str, range_address: str, 
                           expected: List[List[Any]], msg: str = None):
        """
        Assert that range has expected values
        
        Args:
            excel_app: Excel application instance
            sheet_name: Sheet name
            range_address: Range address (e.g., "A1:C5")
            expected: Expected values as 2D list
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        actual = excel_app.get_range_values(sheet_name, range_address)
        if actual != expected:
            error_msg = f"Range values in {sheet_name}!{range_address} don't match expected values"
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)
    
    @staticmethod
    def assert_dataframe_equal(actual_df: pd.DataFrame, expected_df: pd.DataFrame, 
                              check_dtype: bool = True, msg: str = None):
        """
        Assert that dataframes are equal
        
        Args:
            actual_df: Actual dataframe
            expected_df: Expected dataframe
            check_dtype: Whether to check data types
            msg: Optional message
            
        Raises:
            AssertionError: If assertion fails
        """
        try:
            pd.testing.assert_frame_equal(actual_df, expected_df, check_dtype=check_dtype)
        except AssertionError as e:
            error_msg = str(e)
            if msg:
                error_msg = f"{msg}: {error_msg}"
            raise AssertionError(error_msg)