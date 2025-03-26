from setuptools import setup, find_packages

setup(
    name="vba-test-framework",
    version="0.1.0",
    description="Python framework for testing VBA macros",
    author="Your Name",
    author_email="your.email@example.com",
    packages=find_packages(),
    install_requires=[
        "xlwings>=0.33.11",
        "pandas>=1.3.0",
        "openpyxl>=3.0.9",
        "pytest>=7.0.0",
    ],
    entry_points={
        "console_scripts": [
            "vbatest=vba_test_framework.cli:main",
        ],
    },
    python_requires=">=3.7",
    classifiers=[
        "Development Status :: 3 - Alpha",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: MIT License",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
    ],
)