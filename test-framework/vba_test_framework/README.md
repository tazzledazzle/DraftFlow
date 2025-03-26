# VBA Testing Framework

A Python framework for automated testing of VBA macros in Excel workbooks.

## Features

- Run VBA macros from Python and verify their results
- Interact with Excel worksheets to set up test data and validate outputs
- Run tests in batch mode with detailed reporting
- Record macro executions to automatically generate test cases
- Command-line interface for running tests and creating test reports

## Installation

### Prerequisites

- Python 3.7+
- Windows operating system with Excel installed

### Install from source

```bash
# Clone repository
git clone https://github.com/yourusername/vba_test_framework.git
cd vba_test_framework

# Install the package
pip install -e .
```

## Quick Start

### 1. Create a Simple Test

```python
import os
from vba_test_framework import VBATestCase, vba_test
from vba_assertions import VBAAssertions

class MyFirstTest(VBATestCase):
    """Test case for Excel calculation macros"""
    
    @vba_test
    def test_sum_macro(self):
        """Test the SumValues macro"""
        # Set up test data
        self.excel.set_cell_value("Sheet1", 1, 1, 10)  # A1 = 10
        self.excel.set_cell_value("Sheet1", 2, 1, 20)  # A2 = 20
        
        # Run the macro
        self.excel.run_macro("SumValues")
        
        # Verify the result
        VBAAssertions.assert_cell_value(self.excel, "Sheet1", 3, 1, 30)  # A3 should be 30

if __name__ == "__main__":
    from vba_test_framework import create_test_suite_from_class
    
    # Path to Excel file with VBA code
    excel_path = r"C:\path\to\your\workbook.xlsx"  # Update with your file path
    
    # Create and run the test suite
    test_suite = create_test_suite_from_class(
        MyFirstTest, 
        "My First VBA Test", 
        excel_path
    )
    
    # Run tests
    results = test_suite.run()
    
    # Generate HTML report
    report_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "test_report.html")
    test_suite.generate_report(results, report_path)
    
    print(f"Tests: {results['total']}, Passed: {results['passed']}, Failed: {results['failed']}")
```

### 2. Record a Test

```bash
# Record a macro execution and generate a test case
vbatest record -e "C:\path\to\your\workbook.xlsx" -m "YourMacroName" --output "tests/recorded_test.py" --input-range "Sheet1:A1:B5" --output-range "Sheet1:C1:C5"
```

### 3. Run Tests from Command Line

```bash
# Run all tests in tests directory
vbatest run -d tests -e "C:\path\to\your\workbook.xlsx" -r reports

# Run specific test class
vbatest run -d tests -t MySpecificTest -e "C:\path\to\your\workbook.xlsx" -r reports
```

## Project Structure

```
vba_test_framework/
├── vba_test_framework/
│   ├── __init__.py
│   ├── core.py                   # Main framework classes
│   ├── assertions.py             # Assertion utilities
│   ├── recorder.py               # Record macro executions
│   └── cli.py                    # Command-line interface
├── tests/
│   ├── __init__.py
│   ├── example_test.py
│   └── recorded_tests/
├── examples/
│   ├── basic_test.py
│   └── workbooks/
├── setup.py
└── README.md
```

## Documentation

### Core Classes

#### `ExcelApplication`

Manages Excel application instances for testing.

```python
excel = ExcelApplication(visible=True)  # Create Excel instance with UI visible
excel.start()  # Start Excel
excel.open_workbook("path/to/workbook.xlsx")  # Open workbook
excel.run_macro("MacroName", arg1, arg2)  # Run macro with arguments
excel.get_cell_value("Sheet1", 1, 1)  # Get value from cell A1
excel.set_cell_value("Sheet1", 1, 1, "New Value")  # Set value in cell A1
excel.quit()  # Close Excel
```

#### `VBATestCase`

Base class for creating test cases.

```python
class MyTest(VBATestCase):
    @vba_test
    def test_something(self):
        # Test code here
        pass
```

#### `VBATestSuite`

Runs collections of test cases.

```python
suite = VBATestSuite("My Tests", "path/to/workbook.xlsx")
suite.add_test_case(test_case_instance)
results = suite.run()
suite.generate_report(results, "report.html")
```

### Assertions

The `VBAAssertions` class provides various assertion methods:

```python
VBAAssertions.assert_equal(actual, expected)
VBAAssertions.assert_cell_value(excel, "Sheet1", 1, 1, expected_value)
VBAAssertions.assert_range_values(excel, "Sheet1", "A1:B5", expected_range)
VBAAssertions.assert_almost_equal(actual, expected, precision=2)
```

### Recorder

Record VBA macro executions:

```python
recorder = VBARecorder("path/to/workbook.xlsx")
recorder.start()
recorder.record_macro("MacroName", args=[arg1, arg2])
recorder.generate_test_class("RecordedTest", "recorded_test.py")
recorder.stop()
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.