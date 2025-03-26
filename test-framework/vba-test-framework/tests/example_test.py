import os
import sys
from pathlib import Path

# Add parent directory to path for imports
sys.path.append(str(Path(__file__).parent.parent))

from vba_test_framework import VBATestCase, vba_test, create_test_suite_from_class
from vba_assertions import VBAAssertions

class CalculationTest(VBATestCase):
    """Test case for Excel calculation macros"""
    
    @vba_test
    def test_sum_function(self):
        """Test the SumValues macro"""
        # Set up test data
        self.excel.set_cell_value("Sheet1", 1, 1, 10)  # A1 = 10
        self.excel.set_cell_value("Sheet1", 2, 1, 20)  # A2 = 20
        self.excel.set_cell_value("Sheet1", 3, 1, 30)  # A3 = 30
        
        # Run the macro - assume it sums A1:A3 and puts result in A4
        self.excel.run_macro("SumValues")
        
        # Verify the result
        VBAAssertions.assert_cell_value(self.excel, "Sheet1", 4, 1, 60)  # A4 should be 60
    
    @vba_test
    def test_multiply_function(self):
        """Test the MultiplyValues macro"""
        # Set up test data
        self.excel.set_cell_value("Sheet1", 1, 2, 5)   # B1 = 5
        self.excel.set_cell_value("Sheet1", 2, 2, 4)   # B2 = 4
        
        # Run the macro with arguments - assume it multiplies values and returns result
        result = self.excel.run_macro("MultiplyValues", "B1", "B2")
        
        # Verify the result
        VBAAssertions.assert_equal(result, 20)
    
    @vba_test
    def test_formula_creation(self):
        """Test the CreateFormula macro"""
        # Set up test data
        self.excel.set_cell_value("Sheet1", 5, 1, 100)  # A5 = 100
        self.excel.set_cell_value("Sheet1", 5, 2, 50)   # B5 = 50
        
        # Run the macro - assume it creates a formula in C5
        self.excel.run_macro("CreateFormula", 5)
        
        # Verify the result
        VBAAssertions.assert_cell_value(self.excel, "Sheet1", 5, 3, 150)  # C5 should be 150
        VBAAssertions.assert_cell_formula(self.excel, "Sheet1", 5, 3, "=A5+B5")

if __name__ == "__main__":
    # Path to Excel file with VBA code
    excel_path = r"C:\path\to\your\workbook.xlsx"  # Update with your file path
    
    # Create and run the test suite
    test_suite = create_test_suite_from_class(
        CalculationTest, 
        "Calculation Macros Test", 
        excel_path,
        visible=True  # Set to True to see Excel during tests
    )
    
    # Run tests
    results = test_suite.run()
    
    # Generate HTML report
    report_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "calculation_test_report.html")
    test_suite.generate_report(results, report_path)
    
    # Print summary
    print(f"Tests: {results['total']}, Passed: {results['passed']}, Failed: {results['failed']}, Errors: {results['errors']}")
    print(f"Report generated at {report_path}")