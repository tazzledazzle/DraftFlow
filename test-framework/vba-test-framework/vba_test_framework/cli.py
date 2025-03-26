#!/usr/bin/env python
import os
import sys
import argparse
import importlib.util
import inspect
import logging
import json
from pathlib import Path
from datetime import datetime
import glob

# Add parent directory to path for imports
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

from  vba_test_framework   import VBATestCase, VBATestSuite, create_test_suite_from_class
from vba_recorder import VBARecorder
from macro_detector import MacroDetector
from excel_factory import ExcelBackendFactory

def setup_logging(verbose):
    """Set up logging based on verbosity level"""
    log_level = logging.DEBUG if verbose else logging.INFO
    log_file = os.path.join(current_dir, "vba_test_cli.log")

    logging.basicConfig(
        level=log_level,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler(log_file),
            logging.StreamHandler()
        ]
    )

def discover_test_classes(test_dir, pattern="*_test.py"):
    """
    Discover all test classes in the specified directory
    
    Args:
        test_dir: Directory to search for test files
        pattern: Pattern to match test files
        
    Returns:
        Dictionary mapping class names to (module, class) tuples
    """
    test_classes = {}

    # Find all test files
    test_files = glob.glob(os.path.join(test_dir, pattern))

    for file_path in test_files:
        # Import the module
        module_name = os.path.basename(file_path).replace(".py", "")
        spec = importlib.util.spec_from_file_location(module_name, file_path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)

        # Find test classes
        for name, obj in inspect.getmembers(module):
            if inspect.isclass(obj) and issubclass(obj, VBATestCase) and obj != VBATestCase:
                test_classes[name] = (module, obj)

    return test_classes

def run_tests(args):
    """Run tests based on command-line arguments"""
    # Set up logging
    setup_logging(args.verbose)

    # We now exclusively use xlwings as the backend
    backend = "xlwings"

    # If specific test(s) specified
    if args.test:
        test_classes = discover_test_classes(args.dir)

        # Check if specified tests exist
        for test_name in args.test:
            if test_name not in test_classes:
                logging.error(f"Test class '{test_name}' not found")
                return 1

        # Run only specified tests
        results = []
        for test_name in args.test:
            module, cls = test_classes[test_name]

            # Get Excel file path
            if hasattr(cls, 'workbook_path'):
                excel_path = cls.workbook_path
            elif args.excel:
                excel_path = args.excel
            else:
                logging.error(f"Excel file path not specified for test '{test_name}'")
                continue

            # Create and run test suite
            test_suite = create_test_suite_from_class(
                cls,
                test_name,
                excel_path,
                visible=args.visible,
                backend=backend
            )

            # Run tests
            result = test_suite.run()
            results.append(result)

            # Generate report
            if args.report:
                report_dir = args.report
                os.makedirs(report_dir, exist_ok=True)
                report_path = os.path.join(report_dir, f"{test_name}_report.html")
                test_suite.generate_report(result, report_path)
                logging.info(f"Report generated at {report_path}")

    # Run all tests in directory
    else:
        test_classes = discover_test_classes(args.dir)
        if not test_classes:
            logging.error(f"No test classes found in '{args.dir}'")
            return 1

        # Run all discovered tests
        results = []
        for test_name, (module, cls) in test_classes.items():
            # Get Excel file path
            if hasattr(cls, 'workbook_path'):
                excel_path = cls.workbook_path
            elif args.excel:
                excel_path = args.excel
            else:
                logging.error(f"Excel file path not specified for test '{test_name}'")
                continue

            # Create and run test suite
            test_suite = create_test_suite_from_class(
                cls,
                test_name,
                excel_path,
                visible=args.visible,
                backend=backend
            )

            # Run tests
            result = test_suite.run()
            results.append(result)

            # Generate report
            if args.report:
                report_dir = args.report
                os.makedirs(report_dir, exist_ok=True)
                report_path = os.path.join(report_dir, f"{test_name}_report.html")
                test_suite.generate_report(result, report_path)
                logging.info(f"Report generated at {report_path}")

    # Print summary
    total_tests = sum(r["total"] for r in results)
    total_passed = sum(r["passed"] for r in results)
    total_failed = sum(r["failed"] for r in results)
    total_errors = sum(r["errors"] for r in results)

    print("\n=== Test Summary ===")
    print(f"Total Test Classes: {len(results)}")
    print(f"Total Tests: {total_tests}")
    print(f"Passed: {total_passed}")
    print(f"Failed: {total_failed}")
    print(f"Errors: {total_errors}")

    # Generate summary report
    if args.report:
        summary_path = os.path.join(args.report, "summary_report.html")
        generate_summary_report(results, summary_path)
        print(f"\nSummary report generated at {summary_path}")

    # Return non-zero if any tests failed
    return 1 if total_failed + total_errors > 0 else 0

def record_test(args):
    """Record VBA macro execution and generate test case"""
    # Set up logging
    setup_logging(args.verbose)

    if not args.excel:
        logging.error("Excel file path not specified")
        return 1

    if not args.macro:
        logging.error("Macro name not specified")
        return 1

    recorder = VBARecorder(args.excel, visible=True)

    try:
        # Start recording
        logging.info(f"Starting recording for macro '{args.macro}'")
        if not recorder.start():
            return 1

        # Parse input ranges
        input_ranges = []
        if args.input_range:
            for range_str in args.input_range:
                parts = range_str.split(":")
                if len(parts) == 2:
                    input_ranges.append({
                        "sheet": parts[0],
                        "range": parts[1]
                    })

        # Parse output ranges
        output_ranges = []
        if args.output_range:
            for range_str in args.output_range:
                parts = range_str.split(":")
                if len(parts) == 2:
                    output_ranges.append({
                        "sheet": parts[0],
                        "range": parts[1]
                    })

        # Parse macro arguments
        macro_args = []
        if args.args:
            for arg_str in args.args:
                # Try to convert to appropriate type
                if arg_str.lower() == "true":
                    macro_args.append(True)
                elif arg_str.lower() == "false":
                    macro_args.append(False)
                elif arg_str.isdigit():
                    macro_args.append(int(arg_str))
                elif arg_str.replace(".", "", 1).isdigit():
                    macro_args.append(float(arg_str))
                else:
                    macro_args.append(arg_str)

        # Record macro execution
        success = recorder.record_macro(
            args.macro,
            args=macro_args,
            input_ranges=input_ranges,
            output_ranges=output_ranges
        )

        if not success:
            logging.error("Failed to record macro execution")
            return 1

        # Generate test class
        if args.output:
            # Generate test class name
            class_name = args.name if args.name else f"{args.macro.replace(' ', '')}Test"

            # Generate test class
            code = recorder.generate_test_class(class_name, args.output)
            logging.info(f"Test class generated and saved to {args.output}")

            # Save recording if requested
            if args.save_recording:
                recording_path = os.path.splitext(args.output)[0] + ".json"
                recorder.save_recording(recording_path)
                logging.info(f"Recording saved to {recording_path}")

    finally:
        # Stop recording
        recorder.stop()

    return 0

def detect_macros(args):
    """Detect macros in Excel workbook"""
    # Set up logging
    setup_logging(args.verbose)

    if not args.excel:
        logging.error("Excel file path not specified")
        return 1

    # Create macro detector
    detector = MacroDetector(use_xlwings=args.backend.lower() == "xlwings")

    # Detect macros
    macros = detector.detect_macros(args.excel)

    if not macros:
        print(f"No macros detected in '{args.excel}'")
        return 0

    print(f"\nDetected {len(macros)} macros in '{args.excel}':")

    # Group macros by module
    by_module = {}
    for full_name, info in macros.items():
        if info.module not in by_module:
            by_module[info.module] = []
        by_module[info.module].append(info)

    # Print macros by module
    for module, module_macros in by_module.items():
        print(f"\nModule: {module}")
        for macro in module_macros:
            print(f"  {macro.signature}")
            if macro.description:
                description = macro.description.replace("\n", "\n      ")
                print(f"      {description}")

    # Generate JSON output
    if args.json:
        json_output = {}
        for full_name, info in macros.items():
            json_output[full_name] = {
                "name": info.name,
                "module": info.module,
                "is_function": info.is_function,
                "return_type": info.return_type,
                "parameters": [
                    {
                        "name": p.name,
                        "type": p.param_type,
                        "optional": p.optional,
                        "default_value": p.default_value
                    }
                    for p in info.parameters
                ],
                "description": info.description,
                "signature": info.signature
            }

        with open(args.json, 'w') as f:
            json.dump(json_output, f, indent=2)

        print(f"\nMacro information saved to '{args.json}'")

    # Generate test stubs
    if args.generate_tests:
        output_dir = args.generate_tests
        generated_files = detector.generate_test_stubs(macros, output_dir)

        print(f"\nGenerated {len(generated_files)} test stub files:")
        for file_path in generated_files:
            print(f"  {file_path}")

    return 0

def generate_summary_report(results, output_file):
    """
    Generate HTML summary report

    Args:
        results: List of test results
        output_file: Path to save HTML report
    """
    # Calculate summary statistics
    total_tests = sum(r["total"] for r in results)
    total_passed = sum(r["passed"] for r in results)
    total_failed = sum(r["failed"] for r in results)
    total_errors = sum(r["errors"] for r in results)

    # Generate HTML
    html = f"""<!DOCTYPE html>
<html>
<head>
    <title>VBA Test Summary Report</title>
    <style>
        body {{ font-family: Arial, sans-serif; margin: 20px; }}
        h1 {{ color: #333366; }}
        .summary {{ margin: 20px 0; padding: 10px; background-color: #f5f5f5; border-radius: 5px; }}
        .test-suite {{ margin: 10px 0; padding: 10px; border-radius: 5px; }}
        .test-suite-header {{ display: flex; justify-content: space-between; }}
        .success {{ background-color: #dff0d8; }}
        .failure {{ background-color: #f2dede; }}
        .mixed {{ background-color: #fcf8e3; }}
        .details {{ margin-top: 5px; font-family: monospace; white-space: pre-wrap; }}
        
        .progress-bar {{
            height: 20px;
            width: 100%;
            background-color: #f5f5f5;
            border-radius: 5px;
            margin-top: 5px;
        }}
        
        .progress {{
            height: 100%;
            border-radius: 5px;
            background-color: #5cb85c;
        }}
    </style>
</head>
<body>
    <h1>VBA Test Summary Report</h1>
    <p>Generated on {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}</p>
    
    <div class="summary">
        <h2>Summary</h2>
        <p>Test Suites: {len(results)}</p>
        <p>Total Tests: {total_tests}</p>
        <p>Passed: {total_passed}</p>
        <p>Failed: {total_failed}</p>
        <p>Errors: {total_errors}</p>
        
        <div class="progress-bar">
            <div class="progress" style="width: {int((total_passed / total_tests) * 100) if total_tests > 0 else 0}%;"></div>
        </div>
    </div>
    
    <h2>Test Suites</h2>
"""

    # Add each test suite
    for result in results:
        # Determine result class
        if result["failed"] + result["errors"] == 0:
            result_class = "success"
        elif result["passed"] == 0:
            result_class = "failure"
        else:
            result_class = "mixed"

        html += f"""
    <div class="test-suite {result_class}">
        <div class="test-suite-header">
            <h3>{result["name"]}</h3>
            <div>
                Passed: {result["passed"]}/{result["total"]} 
                ({int((result["passed"] / result["total"]) * 100) if result["total"] > 0 else 0}%)
            </div>
        </div>
        
        <div class="progress-bar">
            <div class="progress" style="width: {int((result["passed"] / result["total"]) * 100) if result["total"] > 0 else 0}%;"></div>
        </div>
        
        <h4>Test Cases</h4>
        <ul>
"""

        # Add each test case
        for test_result in result["test_results"]:
            status_class = "success" if test_result["status"] == "PASSED" else "failure"

            html += f"""
            <li class="{status_class}">
                {test_result["name"]} - {test_result["status"]} ({test_result["execution_time"]:.2f}s)
"""

            if test_result["error"]:
                html += f"""
                <div class="details">
                    {test_result["error"]}
                </div>
"""

            html += "            </li>\n"

        html += "        </ul>\n    </div>\n"

    html += """
</body>
</html>
"""

    # Write to file
    with open(output_file, 'w') as f:
        f.write(html)

def main():
    parser = argparse.ArgumentParser(description='VBA Test CLI')
    subparsers = parser.add_subparsers(dest='command', help='Command to run')

    # Common arguments for all commands
    common_parser = argparse.ArgumentParser(add_help=False)
    common_parser.add_argument('-e', '--excel', type=str,
                               help='Path to Excel workbook')
    common_parser.add_argument('--verbose', action='store_true',
                               help='Enable verbose logging')

    # Run tests command
    run_parser = subparsers.add_parser('run', help='Run tests', parents=[common_parser])
    run_parser.add_argument('-d', '--dir', type=str, default='tests',
                            help='Directory containing test files')
    run_parser.add_argument('-t', '--test', type=str, nargs='+',
                            help='Specific test class(es) to run')
    run_parser.add_argument('-r', '--report', type=str,
                            help='Directory to save HTML reports')
    run_parser.add_argument('-v', '--visible', action='store_true',
                            help='Make Excel visible during tests')

    # Record command
    record_parser = subparsers.add_parser('record', help='Record VBA macro execution', parents=[common_parser])
    record_parser.add_argument('-m', '--macro', type=str, required=True,
                               help='Name of macro to record')
    record_parser.add_argument('-a', '--args', type=str, nargs='+',
                               help='Arguments to pass to macro')
    record_parser.add_argument('-i', '--input-range', type=str, nargs='+',
                               help='Input ranges to record (format: Sheet:Range)')
    record_parser.add_argument('-o', '--output-range', type=str, nargs='+',
                               help='Output ranges to record (format: Sheet:Range)')
    record_parser.add_argument('--output', type=str,
                               help='Path to save generated test class')
    record_parser.add_argument('-n', '--name', type=str,
                               help='Name for generated test class')
    record_parser.add_argument('-s', '--save-recording', action='store_true',
                               help='Save recording to JSON file')

    # Detect command
    detect_parser = subparsers.add_parser('detect', help='Detect VBA macros', parents=[common_parser])
    detect_parser.add_argument('--json', type=str,
                               help='Path to save detected macros as JSON')
    detect_parser.add_argument('--generate-tests', type=str,
                               help='Directory to generate test stubs')

    args = parser.parse_args()

    if args.command == 'run':
        return run_tests(args)
    elif args.command == 'record':
        return record_test(args)
    elif args.command == 'detect':
        return detect_macros(args)
    else:
        parser.print_help()
        return 1

if __name__ == "__main__":
    sys.exit(main())