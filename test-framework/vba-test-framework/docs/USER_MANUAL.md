# Using the VBA Testing Framework: Practical Guide

Now that we've built the complete VBA testing framework, let's go through a practical workflow of how to use it effectively.

## Workflow: Creating and Running Tests

### 1. Install the Framework

First, install the framework from your local directory:

```bash
# Navigate to the directory where you placed the code
cd path/to/vba-test-framework

# Install in development mode
pip install -e .
```

### 2. Recording Tests (Optional)

For quick test creation, use the recorder:

```bash
# Record a specific macro execution
vbatest record -e "C:\path\to\excel_file.xlsx" -m "SumValues" \
    --input-range "Sheet1:A1:A3" --output-range "Sheet1:A4" \
    --output "tests/recorded_sum_test.py"
```

### 3. Write Tests Manually

For more control, write tests directly:

```python
from vba_test_framework import VBATestCase, vba_test
from vba_assertions import VBAAssertions

class DataValidationTest(VBATestCase):
    @vba_test
    def test_data_validation(self):
        # Setup test data
        self.excel.set_cell_value("Sheet1", 1, 1, "Invalid Value")
        
        # Run data validation macro
        result = self.excel.run_macro("ValidateData") 
        
        # Verify the result
        VBAAssertions.assert_false(result, "Validation should fail for invalid data")
```

### 4. Run the Tests

Run your tests using the command line interface:

```bash
# Run a specific test
vbatest run -t DataValidationTest -e "path/to/excel_file.xlsx" -r reports

# Run all tests in a directory
vbatest run -d tests -e "path/to/excel_file.xlsx" -r reports
```

### 5. Analyze Test Reports

Open the generated HTML reports to view test results, including:
- Overall pass/fail status
- Execution time
- Error details for failed tests
- Test coverage summary

## Advanced Testing Patterns

### 1. Parameterized Tests

For testing the same functionality with different inputs:

```python
@vba_test
def test_tax_calculation(self):
    # Test tax calculation with different rates
    test_cases = [
        {"amount": 100, "rate": 0.05, "expected": 5},
        {"amount": 100, "rate": 0.10, "expected": 10},
        {"amount": 50, "rate": 0.05, "expected": 2.5}
    ]
    
    for case in test_cases:
        # Setup test data
        self.excel.set_cell_value("Sheet1", 1, 1, case["amount"])
        self.excel.set_cell_value("Sheet1", 1, 2, case["rate"])
        
        # Run calculation
        self.excel.run_macro("CalculateTax")
        
        # Verify the result
        result = self.excel.get_cell_value("Sheet1", 1, 3)
        VBAAssertions.assert_almost_equal(
            result, 
            case["expected"], 
            precision=2,
            msg=f"Failed with amount={case['amount']}, rate={case['rate']}"
        )
```

### 2. Testing Complex Data Structures

For testing macros that process large datasets:

```python
@vba_test
def test_data_import(self):
    # Create sample data
    sample_data = [
        ["ID", "Name", "Value"],
        [1, "Item A", 100],
        [2, "Item B", 200],
        [3, "Item C", 300]
    ]
    
    # Prepare import range
    self.excel.set_range_values("Sheet1", "A1:C4", sample_data)
    
    # Run import macro
    self.excel.run_macro("ImportAndProcess")
    
    # Verify results in summary area
    VBAAssertions.assert_cell_value(self.excel, "Sheet1", 10, 1, "Total Items")
    VBAAssertions.assert_cell_value(self.excel, "Sheet1", 10, 2, 3)
    VBAAssertions.assert_cell_value(self.excel, "Sheet1", 11, 1, "Total Value")
    VBAAssertions.assert_cell_value(self.excel, "Sheet1", 11, 2, 600)
```

### 3. Testing Error Handling

For testing how macros handle errors:

```python
@vba_test
def test_error_handling(self):
    # Deliberately create an error condition
    self.excel.set_cell_value("Sheet1", 1, 1, "text")  # Non-numeric value
    
    # Run macro with expected error
    result = self.excel.run_macro("DivideValues")
    
    # Verify the macro handled the error correctly
    VBAAssertions.assert_equal(result, "#ERROR")
    VBAAssertions.assert_cell_value(self.excel, "Sheet1", 5, 1, "Error Log")
    VBAAssertions.assert_cell_value(self.excel, "Sheet1", 5, 2, "Type Mismatch")
```

## Integration with CI/CD

You can integrate the VBA tests with CI/CD systems like Jenkins or GitHub Actions:

```yaml
# Example GitHub Actions workflow
name: VBA Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up Python
        uses: actions/setup-python@v2
        with:
          python-version: '3.9'
      
      - name: Install dependencies
        run: |
          python -m pip install --upgrade pip
          pip install -e .
      
      - name: Install Office
        uses: microsoft/setup-msbuild@v1.1
      
      - name: Run VBA tests
        run: |
          vbatest run -d tests -e "examples/workbooks/product_management.xlsx" -r reports
      
      - name: Upload test reports
        uses: actions/upload-artifact@v2
        with:
          name: test-reports
          path: reports/
```

## Debugging Failed Tests

When tests fail, use these techniques to diagnose the issues:

1. **Enable Excel visibility**: Add `-v` or `--visible` flag to see Excel during test execution
2. **Add debug statements**: Use logging to track values during test execution
3. **Step through tests**: Run tests in debug mode with `--debug` flag (requires adding debug mode to the framework)
4. **Check test report**: Review detailed error messages in the generated HTML reports

## Next Steps

To further enhance your VBA testing framework:

1. Add code coverage analysis for VBA code
2. Create a plugin system for custom assertions
3. Add support for Excel events and UI interactions
4. Implement performance testing for slow macros
5. Add support for comparing Excel files (before/after testing)

With this framework, you can now create robust automated tests for your VBA macros, ensuring reliability and easier maintenance of your Excel applications.