"""
Example script demonstrating the integrated VBA testing framework
with xlwings integration and macro detection.

This script shows a complete workflow:
1. Detect macros in an Excel workbook
2. Generate test stubs for detected macros
3. Run the generated tests using xlwings backend
"""
import os
import sys
import logging
from pathlib import Path

# Add parent directory to path for imports
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

from macro_detector import MacroDetector
from vba_test_framework import create_test_suite_from_class
from excel_factory import ExcelBackendFactory
import importlib.util
import inspect

def setup_logging():
    """Set up logging"""
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler("integration_example.log"),
            logging.StreamHandler()
        ]
    )

def detect_and_generate_tests(excel_path, output_dir):
    """
    Detect macros in Excel workbook and generate test stubs

    Args:
        excel_path: Path to Excel workbook
        output_dir: Directory to save test stubs

    Returns:
        List of generated test file paths
    """
    print(f"Detecting macros in '{excel_path}'...")
    detector = MacroDetector()
    macros = detector.detect_macros(excel_path)

    if not macros:
        print("No macros detected.")
        return []

    print(f"Detected {len(macros)} macros.")

    # Generate test stubs
    print(f"Generating test stubs in '{output_dir}'...")
    generated_files = detector.generate_test_stubs(macros, output_dir)

    print(f"Generated {len(generated_files)} test stub files:")
    for file_path in generated_files:
        print(f"  {file_path}")

    return generated_files

def load_test_class(file_path):
    """
    Load a test class from a file

    Args:
        file_path: Path to Python file

    Returns:
        Test class or None if not found
    """
    try:
        # Import the module
        module_name = os.path.basename(file_path).replace(".py", "")
        spec = importlib.util.spec_from_file_location(module_name, file_path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)

        # Find test classes
        for name, obj in inspect.getmembers(module):
            if inspect.isclass(obj) and hasattr(obj, 'setup') and hasattr(obj, 'teardown'):
                return obj

        return None
    except Exception as e:
        logging.error(f"Error loading test class from {file_path}: {e}")
        return None

def update_workbook_path(test_file, excel_path):
    """
    Update the workbook path in a test file

    Args:
        test_file: Path to test file
        excel_path: Path to Excel workbook
    """
    try:
        with open(test_file, 'r') as f:
            content = f.read()

        # Replace the placeholder path
        updated_content = content.replace(
            'excel_path = r"path/to/your/workbook.xlsx"',
            f'excel_path = r"{excel_path}"'
        )

        # Write back to file
        with open(test_file, 'w') as f:
            f.write(updated_content)

        logging.info(f"Updated workbook path in {test_file}")
    except Exception as e:
        logging.error(f"Error updating workbook path in {test_file}: {e}")

def run_test(test_class, excel_path, report_dir):
    """
    Run a test class

    Args:
        test_class: Test class to run
        excel_path: Path to Excel workbook
        report_dir: Directory to save report

    Returns:
        Test results
    """
    # Create test suite
    test_suite = create_test_suite_from_class(
        test_class,
        test_class.__name__,
        excel_path,
        visible=True
    )

    # Run tests
    results = test_suite.run()

    # Generate report
    os.makedirs(report_dir, exist_ok=True)
    report_path = os.path.join(report_dir, f"{test_class.__name__}_report.html")
    test_suite.generate_report(results, report_path)

    print(f"Tests: {results['total']}, Passed: {results['passed']}, Failed: {results['failed']}, Errors: {results['errors']}")
    print(f"Report generated at {report_path}")

    return results

def main():
    """Main function demonstrating the integrated workflow"""
    setup_logging()

    # Configuration
    excel_path = os.path.abspath("examples/workbooks/product_management.xlsx")
    test_dir = "generated_tests"
    report_dir = "reports"

    # Step 1: Detect macros and generate test stubs
    generated_files = detect_and_generate_tests(excel_path, test_dir)

    if not generated_files:
        print("No test files generated. Exiting.")
        return 1

    # Step 2: Update workbook paths in generated tests
    for file_path in generated_files:
        update_workbook_path(file_path, excel_path)

    # Step 3: Load and run each test
    all_results = []

    for file_path in generated_files:
        test_class = load_test_class(file_path)
        if test_class:
            print(f"\nRunning tests from {os.path.basename(file_path)}...")
            results = run_test(test_class, excel_path, report_dir)
            all_results.append(results)

    # Step 4: Print overall summary
    total_tests = sum(r["total"] for r in all_results)
    total_passed = sum(r["passed"] for r in all_results)
    total_failed = sum(r["failed"] for r in all_results)
    total_errors = sum(r["errors"] for r in all_results)

    print("\n=== Overall Test Summary ===")
    print(f"Total Test Classes: {len(all_results)}")
    print(f"Total Tests: {total_tests}")
    print(f"Passed: {total_passed}")
    print(f"Failed: {total_failed}")
    print(f"Errors: {total_errors}")

    return 0

if __name__ == "__main__":
    sys.exit(main())