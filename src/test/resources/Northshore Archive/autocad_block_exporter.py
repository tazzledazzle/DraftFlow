import logging
import platform
import subprocess
import winreg

import ezdxf
from ezdxf.addons import odafc
import csv
from datetime import datetime
import os
from typing import Dict, List, Any, Optional
from pathlib import Path

class MSIInstaller:
    def __init__(self, msi_path: str, log_file: str = "msi_install.log"):
        self.msi_path = os.path.abspath(msi_path)
        self.log_file = log_file

        # Set up logging
        logging.basicConfig(
            filename=log_file,
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s'
        )
        self.logger = logging.getLogger(__name__)

    def install_msiexec(self, silent: bool = True, properties: dict = None) -> bool:
        """
        Install MSI using msiexec.exe

        Args:
            silent (bool): Run installation silently if True
            properties (dict): Additional MSI properties to pass to the installer

        Returns:
            bool: True if installation successful, False otherwise
        """
        try:
            if not os.path.exists(self.msi_path):
                raise FileNotFoundError(f"MSI file not found: {self.msi_path}")

            # Build command
            cmd = ['msiexec.exe', '/i', self.msi_path]

            # Add silent installation flags
            if silent:
                cmd.extend(['/quiet', '/norestart'])

            # Add logging
            cmd.extend(['/l*v', self.log_file])

            # Add any additional properties
            if properties:
                for key, value in properties.items():
                    cmd.append(f'{key}={value}')

            self.logger.info(f"Starting installation with command: {' '.join(cmd)}")

            # Run installation
            result = subprocess.run(
                cmd,
                check=True,
                capture_output=True,
                text=True
            )

            self.logger.info("Installation completed successfully")
            return True

        except FileNotFoundError as e:
            self.logger.error(f"MSI file error: {e}")
            raise
        except subprocess.CalledProcessError as e:
            self.logger.error(f"Installation failed with error code {e.returncode}")
            self.logger.error(f"Error output: {e.stderr}")
            return False
        except Exception as e:
            self.logger.error(f"Unexpected error during installation: {e}")
            return False

    def install_win32com(self, silent: bool = True) -> bool:
        """
        Install MSI using Windows Installer COM object

        Args:
            silent (bool): Run installation silently if True

        Returns:
            bool: True if installation successful, False otherwise
        """
        try:
            import win32com.client

            if not os.path.exists(self.msi_path):
                raise FileNotFoundError(f"MSI file not found: {self.msi_path}")

            self.logger.info("Starting installation using Windows Installer")

            # Create Windows Installer object
            installer = win32com.client.Dispatch('WindowsInstaller.Installer')

            # Set installation options
            type_mask = 7 if silent else 1  # 7 = silent install, 1 = basic UI

            # Install the MSI
            installer.InstallProduct(self.msi_path, f"LOGGING={self.log_file} TYPE={type_mask}")

            self.logger.info("Installation completed successfully")
            return True

        except ImportError:
            self.logger.error("pywin32 package not installed")
            raise
        except Exception as e:
            self.logger.error(f"Installation failed: {e}")
            return False


class ODAConverter:

    def __init__(self, oda_path: Optional[str] = None, log_file: Optional[str] = None):
        """
        Initialize the ODA Converter.
        :param oda_path: Path to ODA File Converter
        :param log_file: Path to log file
        """
        self._setup_logging(log_file)
        self.oda_path = oda_path
        self.log_file = log_file

        self.ENVVAR_ODA_PATH = 'ODA_CONVERTER_PATH'

        self.oda_path = oda_path or self._find_windows_oda() or self._find_mac_oda() or self._find_linux_oda()

    def _setup_logging(self, log_file: Optional[str]):

        """Setup logging for the ODA Converter."""
        self.logger = logging.getLogger("ODAConverter")
        self.logger.setLevel(logging.DEBUG)

        formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

        if log_file:
            file_handler = logging.FileHandler(log_file)
            file_handler.setLevel(logging.DEBUG)
            file_handler.setFormatter(formatter)
            self.logger.addHandler(file_handler)

        console_handler = logging.StreamHandler()
        console_handler.setFormatter(formatter)
        self.logger.addHandler(console_handler)

    def find_oda_converter(self):
        oda_path = os.environ.get(self.ENVVAR_ODA_PATH)

        if oda_path and self._verify_oda_path(oda_path):
            self.logger.info(f"Using ODA Converter from environment variable: {oda_path}")
            return oda_path

        system = platform.system().lower()

        if system == 'windows':
            oda_path = Path("tools/ODA/ODAFileConverter 25.12.0/ODAFileConverter.exe")
        elif system == 'linux':
            oda_path = Path("tools/ODAFileConverter_QT6_lnxX64_8.3dll_25.12.AppImage")
        elif system == 'darwin':
            oda_path = Path("tools/ODAFileConverter_QT6_lnxX64_8.3dll_25.12.AppImage")
        else:
            self.logger.error(f"Unsupported system: {system}")
            return None

        if oda_path:
            self.logger.info(f"Found ODA Converter at: {oda_path}")
            # Try to run with --version flag
            result = subprocess.run(
                [oda_path, '--version'],
                capture_output=True,
                text=True,
                timeout=5  # 5 second timeout
            )
            print(result.stdout)
        else:
            self.logger.error("ODA Converter not found.")

        return oda_path

    def _find_windows_oda(self) -> Optional[str]:
        """Find ODA File Converter on Windows systems."""
        # List of possible executable names
        exe_names = ['ODAFileConverter.exe', 'ODAFC.exe']

        # Check Windows Registry first
        try:
            registry_paths = [
                (winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\ODA\ODAFileConverter"),
                (winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\WOW6432Node\ODA\ODAFileConverter"),
                (winreg.HKEY_CURRENT_USER, r"SOFTWARE\ODA\ODAFileConverter")
            ]

            for reg_root, reg_path in registry_paths:
                try:
                    with winreg.OpenKey(reg_root, reg_path) as key:
                        install_path, _ = winreg.QueryValueEx(key, "InstallPath")
                        self.logger.debug(f"Found registry entry: {install_path}")

                        # Check for executable in install path
                        for exe in exe_names:
                            full_path = os.path.join(install_path, exe)
                            if self._verify_oda_path(full_path):
                                return full_path
                except WindowsError:
                    continue

        except Exception as e:
            self.logger.debug(f"Error checking registry: {str(e)}")

        # Common installation paths
        common_paths = [
            os.path.join(os.environ.get('ProgramFiles', ''), 'ODA'),
            os.path.join(os.environ.get('ProgramFiles(x86)', ''), 'ODA'),
            os.path.join(os.environ.get('ProgramFiles', ''), 'Open Design Alliance'),
            os.path.join(os.environ.get('ProgramFiles(x86)', ''), 'Open Design Alliance'),
            os.path.join(os.environ.get('LOCALAPPDATA', ''), 'ODA'),
            r'C:\ODA',
            r'C:\Program Files\ODA',
            r'C:\Program Files (x86)\ODA'
        ]

        # Search common paths
        for base_path in common_paths:
            if os.path.exists(base_path):
                # Search recursively up to 2 levels deep
                for root, dirs, files in os.walk(base_path):
                    if root.count(os.sep) - base_path.count(os.sep) > 2:
                        continue

                    for exe in exe_names:
                        if exe in files:
                            full_path = os.path.join(root, exe)
                            if self._verify_oda_path(full_path):
                                return full_path

        # Check PATH environment variable
        path_dirs = os.environ.get('PATH', '').split(os.pathsep)
        for directory in path_dirs:
            for exe in exe_names:
                full_path = os.path.join(directory, exe)
                if self._verify_oda_path(full_path):
                    return full_path

        return None

    def _find_mac_oda(self) -> Optional[str]:
        """Find ODA File Converter on macOS systems."""
        common_paths = [
            '/Applications/ODAFileConverter.app/Contents/MacOS/ODAFileConverter',
            '/Applications/ODA/ODAFileConverter.app/Contents/MacOS/ODAFileConverter',
            os.path.expanduser('~/Applications/ODAFileConverter.app/Contents/MacOS/ODAFileConverter')
        ]

        # Also check Homebrew and MacPorts locations
        brew_path = subprocess.getoutput('brew --prefix').strip()
        if brew_path and os.path.exists(brew_path):
            common_paths.append(os.path.join(brew_path, 'bin/ODAFileConverter'))

        common_paths.append('/opt/local/bin/ODAFileConverter')  # MacPorts

        for path in common_paths:
            if self._verify_oda_path(path):
                return path

        return None

    def _find_linux_oda(self) -> Optional[str]:
        """Find ODA File Converter on Linux systems."""
        common_paths = [
            '/usr/bin/ODAFileConverter',
            '/usr/local/bin/ODAFileConverter',
            '/opt/oda/ODAFileConverter',
            '/opt/ODA/ODAFileConverter',
            os.path.expanduser('~/.local/bin/ODAFileConverter')
        ]

        # Check common package manager locations
        package_paths = [
            '/snap/bin/ODAFileConverter',
            '/var/lib/snapd/snap/bin/ODAFileConverter',
            '/usr/share/ODA/ODAFileConverter'
        ]

        common_paths.extend(package_paths)

        # Try to find using 'which' command
        try:
            which_result = subprocess.getoutput('which ODAFileConverter').strip()
            if which_result and os.path.exists(which_result):
                common_paths.append(which_result)
        except Exception:
            pass

        for path in common_paths:
            if self._verify_oda_path(path):
                return path

        return None

    def _verify_oda_path(self, path: str) -> bool:
        """
        Verify that the given path points to a valid ODA File Converter executable.

        Args:
            path: Path to verify

        Returns:
            bool: True if path is valid ODA Converter executable
        """
        if not path or not os.path.exists(path):
            return False

        try:
            # Check if file is executable
            if not os.access(path, os.X_OK):
                return False

            # Try to run with --version flag
            result = subprocess.run(
                [path, '--version'],
                capture_output=True,
                text=True,
                timeout=5  # 5 second timeout
            )

            # Check if output contains typical ODA version information
            return ('ODA File Converter' in result.stdout or
                    'ODAFC' in result.stdout or
                    'version' in result.stdout.lower())

        except (subprocess.SubprocessError, OSError) as e:
            self.logger.debug(f"Error verifying ODA path {path}: {str(e)}")
            return False

    def _search_additional_locations(self) -> Optional[str]:
        """
        Search additional possible locations based on common installation patterns.
        """
        # Check for portable installations
        portable_paths = [
            os.path.join(os.getcwd(), 'ODAFileConverter'),
            os.path.join(os.getcwd(), 'tools', 'ODAFileConverter'),
            os.path.join(os.getcwd(), 'bin', 'ODAFileConverter')
        ]

        # Add extension for Windows
        if platform.system().lower() == 'windows':
            portable_paths = [f"{path}.exe" for path in portable_paths]

        for path in portable_paths:
            if self._verify_oda_path(path):
                return path

        return None


class DXFBlockExporter:
    def __init__(self, filename: str):
        """
        Initialize the DXF document.

        Args:
            filename: Path to the DWG/DXF file
        """
        try:
            # Load the drawing
            self.doc = ezdxf.readfile(filename)  ##todo: ensure that it's a dxf file and not a dwg file
            self.modelspace = self.doc.modelspace()
            self.filename = filename
        except Exception as e:
            print(f"Error loading file {filename}: {str(e)}")
            raise

    def get_block_data(self) -> List[Dict[str, Any]]:
        """Extract information about all blocks in the drawing."""
        blocks_data = []

        try:
            # Get the block table
            block_table = self.doc.blocks

            # Process each block
            for block in block_table:
                try:
                    # Skip model and paper space blocks
                    if block.name.startswith('*'):
                        continue

                    # Get block bounds
                    bounds = self._get_block_bounds(block)

                    # Count entities and get attributes
                    entity_count = 0
                    attribute_names = []

                    for entity in block:
                        if entity.dxftype() == 'ATTDEF':
                            attribute_names.append(entity.dxf.tag)
                        else:
                            entity_count += 1

                    # Get extended data (if any)
                    xdata = self._get_xdata(block)

                    block_data = {
                        "Name": block.name,
                        "Description": xdata.get('DESCRIPTION', ''),
                        "Layer": block.dxf.layer if hasattr(block.dxf, 'layer') else '0',
                        "Width": round(bounds['width'], 3) if bounds else 0,
                        "Height": round(bounds['height'], 3) if bounds else 0,
                        "EntityCount": entity_count,
                        "HasAttributes": len(attribute_names) > 0,
                        "AttributeNames": '|'.join(attribute_names),
                        "Units": self._get_units_string(),
                        "LastModified": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                        "Author": xdata.get('AUTHOR', ''),
                        "Category": xdata.get('CATEGORY', ''),
                        "EntityTypes": self._get_entity_types(block)
                    }

                    blocks_data.append(block_data)

                except Exception as e:
                    print(f"Error processing block {block.name}: {str(e)}")
                    continue

        except Exception as e:
            print(f"Error accessing block table: {str(e)}")
            raise

        return blocks_data

    def _get_block_bounds(self, block) -> Dict[str, float]:
        """Calculate the bounding box of a block."""
        try:
            points = []

            for entity in block:
                # Get bounding points based on entity type
                if entity.dxftype() == 'LINE':
                    points.extend([entity.dxf.start, entity.dxf.end])
                elif entity.dxftype() == 'CIRCLE':
                    center = entity.dxf.center
                    radius = entity.dxf.radius
                    points.extend([
                        (center[0] - radius, center[1] - radius),
                        (center[0] + radius, center[1] + radius)
                    ])
                elif entity.dxftype() == 'ARC':
                    center = entity.dxf.center
                    radius = entity.dxf.radius
                    points.extend([
                        (center[0] - radius, center[1] - radius),
                        (center[0] + radius, center[1] + radius)
                    ])
                elif hasattr(entity, 'get_points'):
                    points.extend(entity.get_points())

            if not points:
                return None

            # Calculate bounds
            x_coords = [p[0] for p in points]
            y_coords = [p[1] for p in points]

            return {
                'width': max(x_coords) - min(x_coords),
                'height': max(y_coords) - min(y_coords),
                'min_x': min(x_coords),
                'min_y': min(y_coords),
                'max_x': max(x_coords),
                'max_y': max(y_coords)
            }

        except Exception as e:
            print(f"Error calculating block bounds: {str(e)}")
            return None

    def _get_xdata(self, block) -> Dict[str, str]:
        """Get extended data from a block."""
        xdata = {}
        try:
            if block.has_xdata:
                for tag in block.get_xdata():
                    if len(tag) >= 2:
                        xdata[tag[0]] = tag[1]
        except:
            pass
        return xdata

    def _get_units_string(self) -> str:
        """Get the drawing units as a string."""
        units_map = {
            0: 'None',
            1: 'Inches',
            2: 'Feet',
            3: 'Miles',
            4: 'Millimeters',
            5: 'Centimeters',
            6: 'Meters',
            7: 'Kilometers',
            8: 'Microinches',
            9: 'Mils',
            10: 'Yards',
            11: 'Angstroms',
            12: 'Nanometers',
            13: 'Microns',
            14: 'Decimeters',
            15: 'Dekameters',
            16: 'Hectometers',
            17: 'Gigameters',
            18: 'Astronomical units',
            19: 'Light years',
            20: 'Parsecs'
        }

        units = self.doc.header.get('$INSUNITS', 0)
        return units_map.get(units, 'Unknown')

    def _get_entity_types(self, block) -> str:
        """Get a list of unique entity types in the block."""
        types = set()
        for entity in block:
            types.add(entity.dxftype())
        return '|'.join(sorted(types))

    def export_to_csv(self, output_path: str = None) -> str:
        """
        Export block data to a CSV file.

        Args:
            output_path: Optional path for the CSV file.
                        If not provided, creates file on desktop.

        Returns:
            The path to the created CSV file.
        """
        try:
            # Get block data
            blocks_data = self.get_block_data()

            # If no output path provided, create one on desktop
            if not output_path:
                desktop = str(Path.home() / "Desktop")
                timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                filename = Path(self.filename).stem
                output_path = os.path.join(desktop, f"BlockData_{filename}_{timestamp}.csv")

            # Write to CSV
            with open(output_path, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames = [
                    "Name", "Description", "Layer", "Width", "Height",
                    "EntityCount", "HasAttributes", "AttributeNames",
                    "Units", "LastModified", "Author", "Category",
                    "EntityTypes"
                ]

                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                writer.writerows(blocks_data)

            print(f"Exported {len(blocks_data)} blocks to: {output_path}")
            return output_path

        except Exception as e:
            print(f"Error exporting blocks: {str(e)}")
            raise


def main():
    """Example usage of the block exporter."""
    # ezdxf.options.read_file("my_config.ini")
    try:
        MSIInstaller("")
        ODA_FILE_PATH = ODAConverter().find_oda_converter()
        print(ODA_FILE_PATH)
        input_file = Path("2023-505 1st Ave S (6334)-XREF-Base Details.dwg").absolute()
        doc = odafc.readfile(input_file)
        msp = doc.modelspace()
        print(msp)
        # exporter = DXFBlockExporter(input_file)

        # Export blocks
        # csv_path = exporter.export_to_csv()

        # Show results
        print(f"\nExport completed successfully!")
        # print(f"CSV file created at: {csv_path}")

    except Exception as e:
        print(f"Error: {str(e)}")


if __name__ == "__main__":
    main()
