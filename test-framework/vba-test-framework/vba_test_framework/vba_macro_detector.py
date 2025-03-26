import os
import re
import logging
from typing import Dict, List, Optional, Set, Tuple, Any
from dataclasses import dataclass
import xlwings as xw

@dataclass
class MacroParameter:
    """Represents a parameter of a VBA macro"""
    name: str
    param_type: str = "Variant"  # Default type in VBA
    optional: bool = False
    default_value: Any = None

@dataclass
class MacroInfo:
    """Information about a VBA macro"""
    name: str
    module: str
    is_function: bool = False
    return_type: str = None
    parameters: List[MacroParameter] = None
    description: str = ""

    def __post_init__(self):
        if self.parameters is None:
            self.parameters = []

    @property
    def signature(self) -> str:
        """Return the signature of the macro"""
        params_str = ", ".join([
            f"{p.name}{': ' + p.param_type if p.param_type else ''}"
            + (f" = {p.default_value}" if p.optional else "")
            for p in self.parameters
        ])

        if self.is_function:
            return_type = f" As {self.return_type}" if self.return_type else ""
            return f"Function {self.name}({params_str}){return_type}"
        else:
            return f"Sub {self.name}({params_str})"

class MacroDetector:
    """Utility for detecting and analyzing VBA macros in Excel workbooks"""

    def __init__(self, use_xlwings: bool = True):
        """
        Initialize macro detector

        Args:
            use_xlwings: Parameter kept for backward compatibility but not used
                         as we now exclusively use xlwings
        """
        self.excel_app = None
        self.workbook = None

    def open_workbook(self, file_path: str) -> bool:
        """
        Open an Excel workbook for macro detection

        Args:
            file_path: Path to Excel file

        Returns:
            True if successful, False otherwise
        """
        try:
            abs_path = os.path.abspath(file_path)
            if not os.path.exists(abs_path):
                logging.error(f"File not found: {abs_path}")
                return False

            self.excel_app = xw.App(visible=False, add_book=False)
            self.workbook = self.excel_app.books.open(abs_path)

            logging.info(f"Opened workbook for macro detection: {file_path}")
            return True
        except Exception as e:
            logging.error(f"Failed to open workbook for macro detection: {e}")
            self.close()
            return False

    def close(self):
        """Close the workbook and Excel application"""
        try:
            if self.workbook:
                self.workbook.close()
                self.workbook = None

            if self.excel_app:
                self.excel_app.quit()
                self.excel_app = None

            logging.info("Closed workbook and Excel application")
        except Exception as e:
            logging.error(f"Error closing Excel: {e}")

    def detect_macros(self, file_path: str) -> Dict[str, MacroInfo]:
        """
        Detect all macros in the workbook

        Args:
            file_path: Path to Excel file

        Returns:
            Dictionary of macro names to MacroInfo objects
        """
        result = {}

        try:
            if not self.open_workbook(file_path):
                return result

            # Get VBA project components (modules)
            # With xlwings, we need to access the VBProject via COM
            vb_project = self.workbook.api.VBProject

            # Error handling for protected VBA projects
            try:
                components = vb_project.VBComponents
            except Exception as e:
                logging.error(f"Cannot access VBA project. It may be protected: {e}")
                return result

            # Process each VBA component (module, class, form)
            for i in range(1, components.Count + 1):
                component = components.Item(i)

                # Skip forms and classes for now (focus on standard modules)
                if component.Type not in [1, 2, 3]:  # vbext_ct_StdModule, vbext_ct_ClassModule, vbext_ct_MSForm
                    continue

                # Get the module name and code
                module_name = component.Name
                code_module = component.CodeModule

                # Read the entire code
                line_count = code_module.CountOfLines
                if line_count > 0:
                    code = code_module.Lines(1, line_count)

                    # Parse the code to detect macros
                    macros = self._parse_vba_code(code, module_name)
                    result.update(macros)

            logging.info(f"Detected {len(result)} macros in workbook")
            return result
        except Exception as e:
            logging.error(f"Error detecting macros: {e}")
            return result
        finally:
            self.close()

    def _parse_vba_code(self, code: str, module_name: str) -> Dict[str, MacroInfo]:
        """
        Parse VBA code to detect macros and their properties
        
        Args:
            code: VBA code as string
            module_name: Name of the VBA module
            
        Returns:
            Dictionary of macro names to MacroInfo objects
        """
        result = {}

        # Split code into lines
        lines = code.splitlines()

        # Regular expressions for function and sub declarations
        # Sub/Function Name([param1 As Type, param2, ...]) [As ReturnType]
        sub_pattern = r'(?:Public\s+|Private\s+)?Sub\s+([A-Za-z0-9_]+)\s*\((.*?)\)'
        func_pattern = r'(?:Public\s+|Private\s+)?Function\s+([A-Za-z0-9_]+)\s*\((.*?)\)(?:\s+As\s+([A-Za-z0-9_]+))?'

        # Find all Sub and Function declarations
        line_num = 0
        while line_num < len(lines):
            line = lines[line_num].strip()

            # Check for Sub declaration
            sub_match = re.match(sub_pattern, line)
            if sub_match:
                name = sub_match.group(1)
                params_str = sub_match.group(2).strip()

                # Create macro info
                macro_info = MacroInfo(
                    name=name,
                    module=module_name,
                    is_function=False,
                    parameters=self._parse_parameters(params_str)
                )

                # Look for description in comments above the declaration
                description = self._extract_description(lines, line_num)
                if description:
                    macro_info.description = description

                result[f"{module_name}.{name}"] = macro_info

            # Check for Function declaration
            func_match = re.match(func_pattern, line)
            if func_match:
                name = func_match.group(1)
                params_str = func_match.group(2).strip()
                return_type = func_match.group(3) if func_match.group(3) else None

                # Create macro info
                macro_info = MacroInfo(
                    name=name,
                    module=module_name,
                    is_function=True,
                    return_type=return_type,
                    parameters=self._parse_parameters(params_str)
                )

                # Look for description in comments above the declaration
                description = self._extract_description(lines, line_num)
                if description:
                    macro_info.description = description

                result[f"{module_name}.{name}"] = macro_info

            line_num += 1

        return result

    def _parse_parameters(self, params_str: str) -> List[MacroParameter]:
        """
        Parse VBA parameter string into parameter objects
        
        Args:
            params_str: Parameter string from VBA declaration
            
        Returns:
            List of MacroParameter objects
        """
        if not params_str or params_str.strip() == "":
            return []

        params = []
        # Split by commas, but respect parentheses for nested types
        param_parts = []
        current_part = ""
        paren_level = 0

        for char in params_str:
            if char == ',' and paren_level == 0:
                param_parts.append(current_part.strip())
                current_part = ""
            else:
                if char == '(':
                    paren_level += 1
                elif char == ')':
                    paren_level -= 1
                current_part += char

        if current_part.strip():
            param_parts.append(current_part.strip())

        # Process each parameter
        for part in param_parts:
            # Handle Optional parameters
            optional = False
            default_value = None

            if "Optional" in part:
                optional = True
                part = part.replace("Optional", "").strip()

            # Handle parameters with default values
            if "=" in part:
                name_type, default = part.split("=", 1)
                default_value = default.strip()
                part = name_type.strip()
            else:
                part = part.strip()

            # Handle ByVal and ByRef
            if part.startswith("ByVal "):
                part = part[6:].strip()
            elif part.startswith("ByRef "):
                part = part[6:].strip()

            # Extract parameter name and type
            if " As " in part:
                name, param_type = part.split(" As ", 1)
                param_type = param_type.strip()
            else:
                name = part
                param_type = None

            # Clean up parameter name (no array parentheses)
            name = name.split("(")[0].strip()

            params.append(MacroParameter(
                name=name,
                param_type=param_type,
                optional=optional,
                default_value=default_value
            ))

        return params

    def _extract_description(self, lines: List[str], line_index: int) -> str:
        """
        Extract description from comments above macro declaration
        
        Args:
            lines: All code lines
            line_index: Index of macro declaration line
            
        Returns:
            Description string or empty string if none found
        """
        description_lines = []

        # Look for comments above the function/sub declaration
        i = line_index - 1
        while i >= 0:
            line = lines[i].strip()

            # Stop at blank lines or code
            if not line or not line.startswith("'"):
                break

            # Add comment content (without the ')
            if line.startswith("'"):
                comment = line[1:].strip()
                description_lines.insert(0, comment)

            i -= 1

        return "\n".join(description_lines)

    def generate_test_stubs(self, macro_info: Dict[str, MacroInfo], output_dir: str = "tests") -> List[str]:
        """
        Generate test stub files for detected macros
        
        Args:
            macro_info: Dictionary of macro names to MacroInfo objects
            output_dir: Directory to save test stubs
            
        Returns:
            List of generated file paths
        """
        # Group macros by module
        modules = {}
        for full_name, info in macro_info.items():
            if info.module not in modules:
                modules[info.module] = []
            modules[info.module].append(info)

        # Create output directory if it doesn't exist
        os.makedirs(output_dir, exist_ok=True)

        generated_files = []

        # Generate test file for each module
        for module_name, macros in modules.items():
            file_name = f"{module_name.lower()}_test.py"
            file_path = os.path.join(output_dir, file_name)

            with open(file_path, 'w') as f:
                # Write imports
                f.write(f"""import os
import sys
from pathlib import Path

# Add parent directory to path for imports
sys.path.append(str(Path(__file__).parent.parent))

from vba_test_framework import VBATestCase, vba_test, create_test_suite_from_class
from vba_assertions import VBAAssertions

class {module_name}Test(VBATestCase):
    \"\"\"Test case for {module_name} VBA module\"\"\"
    
    def setup(self):
        \"\"\"Set up test environment\"\"\"
        # Update with your workbook path
        excel_path = r"path/to/your/workbook.xlsx"
        super().setup(excel_path)
    
""")

                # Write test method for each macro
                for macro in macros:
                    # Convert macro name to Python method name
                    method_name = f"test_{macro.name.lower()}"

                    # Create docstring from macro description
                    docstring = macro.description if macro.description else f"Test the {macro.name} {'function' if macro.is_function else 'macro'}"

                    # Write test method
                    f.write(f"    @vba_test\n")
                    f.write(f"    def {method_name}(self):\n")
                    f.write(f"        \"\"\"{docstring}\"\"\"\n")

                    # Set up test data
                    f.write(f"        # TODO: Set up test data\n")

                    # Generate parameter comments
                    if macro.parameters:
                        f.write(f"        # Parameters expected:\n")
                        for param in macro.parameters:
                            type_str = f" ({param.param_type})" if param.param_type else ""
                            f.write(f"        # - {param.name}{type_str}{' (optional)' if param.optional else ''}\n")

                    # Generate code to run the macro
                    if macro.is_function:
                        args = ", ".join(['""' for _ in macro.parameters])
                        f.write(f"\n        # Run the function\n")
                        f.write(f"        result = self.excel.run_macro(\"{macro.module}.{macro.name}\", {args})\n")
                        f.write(f"        \n")
                        f.write(f"        # TODO: Verify the result\n")
                        f.write(f"        # VBAAssertions.assert_equal(result, expected_value)\n")
                    else:
                        args = ", ".join(['""' for _ in macro.parameters])
                        f.write(f"\n        # Run the macro\n")
                        f.write(f"        self.excel.run_macro(\"{macro.module}.{macro.name}\", {args})\n")
                        f.write(f"        \n")
                        f.write(f"        # TODO: Verify the effects\n")
                        f.write(f"        # VBAAssertions.assert_cell_value(self.excel, \"Sheet1\", 1, 1, expected_value)\n")

                    f.write("\n")

                # Write main section
                f.write("""
if __name__ == "__main__":
    # Create and run the test suite
    test_suite = create_test_suite_from_class(
        {0}Test, 
        "{0} Test Suite", 
        visible=True  # Set to True to see Excel during tests
    )
    
    # Run tests
    results = test_suite.run()
    
    # Generate HTML report
    report_dir = "reports"
    os.makedirs(report_dir, exist_ok=True)
    report_path = os.path.join(report_dir, "{1}_test_report.html")
    test_suite.generate_report(results, report_path)
    
    # Print summary
    print(f"Tests: {{results['total']}}, Passed: {{results['passed']}}, Failed: {{results['failed']}}, Errors: {{results['errors']}}")
    print(f"Report generated at {{report_path}}")
""".format(module_name, module_name.lower()))

            generated_files.append(file_path)
            logging.info(f"Generated test stub: {file_path}")

        return generated_files