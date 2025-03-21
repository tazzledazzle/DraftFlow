import unittest
import sys
import os
import argparse


def run_tests(test_file=None, verbose=False, test_pattern=None):
    """Run the Excel VBA macro tests"""
    # Set up the test loader
    loader = unittest.TestLoader()

    # Set up the test suite
    if test_file:
        print(f"Running tests from {test_file}")
        # Import the module if a specific file is specified
        # import importlib.util
        # spec = importlib.util.spec_from_file_location("test_module", test_file)
        # if spec:
        #     test_module = importlib.util.module_from_spec(spec)
        #     spec.loader.exec_module(test_module)
        #     suite = loader.loadTestsFromModule(test_module)
    else:
        # Otherwise discover all tests
        suite = loader.discover(os.path.dirname(__file__), pattern="*test*.py")

    # Filter tests if a pattern is provided
    if test_pattern:
        filtered_suite = unittest.TestSuite()
        for test_case in suite:
            for test in test_case:
                if test_pattern.lower() in test.id().lower():
                    filtered_suite.addTest(test)
        suite = filtered_suite

    # Set up the test runner
    runner = unittest.TextTestRunner(verbosity=2 if verbose else 1)

    # Run the tests
    result = runner.run(suite)

    # Return appropriate exit code
    return 0 if result.wasSuccessful() else 1


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Run Excel VBA macro tests")
    parser.add_argument("-f", "--file", help="Specific test file to run")
    parser.add_argument("-v", "--verbose", action="store_true", help="Verbose output")
    parser.add_argument("-p", "--pattern", help="Only run tests matching pattern")

    args = parser.parse_args()

    sys.exit(run_tests(args.file, args.verbose, args.pattern))
