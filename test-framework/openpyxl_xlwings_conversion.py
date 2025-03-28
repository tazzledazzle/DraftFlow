# Converting openpyxl to xlwings: Comprehensive Guide

"""
This guide demonstrates how to convert common openpyxl operations to xlwings.

xlwings advantages:
- Can interact with open Excel instances
- Easier to work with Excel's object model
- Faster for large data operations in many cases
- Better integration with pandas
- Simpler syntax for many common operations

Note: xlwings requires Excel to be installed on Windows or macOS.
On Linux, it can be used with a compatibility layer.
"""

import xlwings as xw
import pandas as pd
import numpy as np
from datetime import datetime
import os

# ===========================================================================
# 1. WORKBOOK AND WORKSHEET OPERATIONS
# ===========================================================================
def example_workbook_operations():
    print("=== Workbook Operations ===")
    
    # CREATING A NEW WORKBOOK
    # -----------------------
    # openpyxl:
    # from openpyxl import Workbook
    # wb = Workbook()
    # ws = wb.active
    
    # xlwings:
    wb = xw.Book()  # Creates a new workbook
    ws = wb.sheets[0]  # Get the first worksheet
    print(f"Created new workbook with sheet: {ws.name}")
    
    # OPENING AN EXISTING WORKBOOK
    # ----------------------------
    # openpyxl:
    # from openpyxl import load_workbook
    # wb = load_workbook('example.xlsx')
    # ws = wb['Sheet1']
    
    # xlwings:
    # Create example file if it doesn't exist
    if not os.path.exists('example.xlsx'):
        temp_wb = xw.Book()
        temp_wb.save('example.xlsx')
        temp_wb.close()
    
    wb2 = xw.Book('example.xlsx')  # Open an existing workbook
    ws2 = wb2.sheets['Sheet1']  # Get a specific worksheet by name
    print(f"Opened existing workbook: {wb2.name}")
    
    # CREATING A NEW WORKSHEET
    # -----------------------
    # openpyxl:
    # ws2 = wb.create_sheet("Sheet2")
    
    # xlwings:
    ws3 = wb.sheets.add("Sheet2")  # Add a new sheet
    print(f"Added new sheet: {ws3.name}")
    
    # ACCESSING WORKSHEETS
    # -------------------
    # openpyxl:
    # ws = wb.worksheets[0]  # First worksheet by index
    # ws = wb['Sheet1']  # By name
    
    # xlwings:
    ws4 = wb.sheets[0]  # By index
    ws5 = wb.sheets['Sheet2']  # By name
    print(f"Accessed sheets by index ({ws4.name}) and name ({ws5.name})")
    
    # SAVING WORKBOOKS
    # ---------------
    # openpyxl:
    # wb.save('new_file.xlsx')
    
    # xlwings:
    wb.save('new_file.xlsx')
    print("Saved workbook as 'new_file.xlsx'")
    
    # CLOSING WORKBOOKS
    # ----------------
    # openpyxl: (no explicit close method, garbage collection handles it)
    
    # xlwings:
    wb.close()
    wb2.close()
    print("Closed workbooks")


# ===========================================================================
# 2. WORKING WITH CELLS AND RANGES
# ===========================================================================

def example_cell_operations():
    print("\n=== Cell and Range Operations ===")
    
    wb = xw.Book()
    ws = wb.sheets[0]
    
    # WRITING TO CELLS
    # ---------------
    # openpyxl:
    # ws['A1'] = 'Hello'
    # ws.cell(row=2, column=1).value = 'World'
    
    # xlwings:
    ws.range('A1').value = 'Hello'
    ws.range((2, 1)).value = 'World'  # Row 2, Column 1 (1-indexed)
    print("Wrote values to cells A1 and A2")
    
    # READING CELL VALUES
    # ------------------
    # openpyxl:
    # value1 = ws['A1'].value
    # value2 = ws.cell(row=2, column=1).value
    
    # xlwings:
    value1 = ws.range('A1').value
    value2 = ws.range((2, 1)).value
    print(f"Read values: A1 = '{value1}', A2 = '{value2}'")
    
    # WORKING WITH RANGES
    # ------------------
    # openpyxl:
    # for row in ws['A1:B3']:
    #     for cell in row:
    #         cell.value = 'x'
    
    # xlwings:
    ws.range('A3:B5').value = 'x'
    print("Filled range A3:B5 with 'x'")
    
    # WRITING MULTIPLE VALUES AT ONCE
    # ------------------------------
    # openpyxl:
    # for row in range(1, 6):
    #     for col in range(1, 4):
    #         ws.cell(row=row, column=col).value = row * col
    
    # xlwings:
    data = [[row * col for col in range(1, 4)] for row in range(1, 6)]
    ws.range('C1').value = data
    print("Wrote multiplication table to range C1:E5")
    
    # READING RANGES INTO PYTHON LISTS
    # ------------------------------
    # openpyxl:
    # data = []
    # for row in ws['C1:E5']:
    #     row_data = []
    #     for cell in row:
    #         row_data.append(cell.value)
    #     data.append(row_data)
    
    # xlwings:
    read_data = ws.range('C1:E5').value
    print(f"Read data from range (first row): {read_data[0]}")
    
    # PANDAS INTEGRATION
    # -----------------
    # openpyxl doesn't have built-in pandas integration
    
    # xlwings:
    df = pd.DataFrame({
        'Name': ['Alice', 'Bob', 'Charlie'],
        'Age': [25, 30, 35],
        'City': ['New York', 'Boston', 'Chicago']
    })
    
    # Write DataFrame to Excel
    ws.range('A7').value = df
    print("Wrote pandas DataFrame to range A7:C10")
    
    # Read back into DataFrame
    df_read = ws.range('A7').expand().options(pd.DataFrame, header=True).value
    print(f"Read DataFrame back (shape: {df_read.shape})")
    
    wb.close()


# ===========================================================================
# 3. FORMATTING CELLS
# ===========================================================================

def example_formatting_operations():
    print("\n=== Formatting Operations ===")
    
    wb = xw.Book()
    ws = wb.sheets[0]
    
    # CELL FONTS AND STYLES
    # --------------------
    # openpyxl:
    # from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
    # cell = ws['A1']
    # cell.value = "Formatted Cell"
    # cell.font = Font(name='Arial', size=14, bold=True, color='FF0000')
    # cell.fill = PatternFill(start_color="FFFF00", end_color="FFFF00", fill_type="solid")
    # cell.alignment = Alignment(horizontal='center', vertical='center')
    # thin_border = Side(border_style="thin", color="000000")
    # cell.border = Border(top=thin_border, left=thin_border, right=thin_border, bottom=thin_border)
    
    # xlwings:
    cell = ws.range('A1')
    cell.value = "Formatted Cell"
    cell.font.name = 'Arial'
    cell.font.size = 14
    cell.font.bold = True
    cell.font.color = (255, 0, 0)  # RGB for red
    cell.color = (255, 255, 0)  # RGB for yellow
    cell.api.HorizontalAlignment = xw.constants.HAlign.xlHAlignCenter
    cell.api.VerticalAlignment = xw.constants.VAlign.xlVAlignCenter
    cell.api.Borders.Weight = xw.constants.BordersIndex.xlThin
    print("Applied formatting to cell A1")
    
    # NUMBER FORMATS
    # -------------
    # openpyxl:
    # ws['A2'].value = 12345.6789
    # ws['A2'].number_format = '#,##0.00'
    
    # xlwings:
    ws.range('A2').value = 12345.6789
    ws.range('A2').number_format = '#,##0.00'
    print("Applied number format to cell A2")
    
    # DATE FORMATS
    # -----------
    # openpyxl:
    # ws['A3'].value = datetime.now()
    # ws['A3'].number_format = 'yyyy-mm-dd'
    
    # xlwings:
    ws.range('A3').value = datetime.now()
    ws.range('A3').number_format = 'yyyy-mm-dd'
    print("Applied date format to cell A3")
    
    # ADJUSTING COLUMN WIDTH / ROW HEIGHT
    # ---------------------------------
    # openpyxl:
    # ws.column_dimensions['A'].width = 20
    # ws.row_dimensions[1].height = 30
    
    # xlwings:
    ws.range('A:A').column_width = 20
    ws.range('1:1').row_height = 30
    print("Adjusted column width and row height")
    
    # MERGING CELLS
    # ------------
    # openpyxl:
    # ws.merge_cells('B2:D4')
    # ws['B2'] = "Merged Cells"
    
    # xlwings:
    ws.range('B2:D4').merge()
    ws.range('B2').value = "Merged Cells"
    print("Merged cells B2:D4")
    
    wb.close()


# ===========================================================================
# 4. FORMULAS, NAMED RANGES, AND DATA VALIDATION
# ===========================================================================

def example_formula_operations():
    print("\n=== Formulas and Advanced Features ===")
    
    wb = xw.Book()
    ws = wb.sheets[0]
    
    # ADDING FORMULAS
    # --------------
    # openpyxl:
    # ws['A1'] = 10
    # ws['A2'] = 20
    # ws['A3'] = '=SUM(A1:A2)'
    
    # xlwings:
    ws.range('A1').value = 10
    ws.range('A2').value = 20
    ws.range('A3').formula = '=SUM(A1:A2)'
    print(f"Added formula to A3, result: {ws.range('A3').value}")
    
    # NAMED RANGES
    # -----------
    # openpyxl:
    # wb.create_named_range('MyRange', ws, 'A1:A2')
    
    # xlwings:
    wb.names.add('MyRange', '=Sheet1!$A$1:$A$2')
    print("Created named range 'MyRange'")
    
    # Using named range in formula
    ws.range('A4').formula = '=SUM(MyRange)'
    print(f"Used named range in formula, result: {ws.range('A4').value}")
    
    # DATA VALIDATION
    # --------------
    # openpyxl:
    # from openpyxl.worksheet.datavalidation import DataValidation
    # dv = DataValidation(type="list", formula1='"Option1,Option2,Option3"')
    # ws.add_data_validation(dv)
    # dv.add('B1:B5')
    
    # xlwings:
    # Note: For advanced validation, xlwings often uses Excel's API directly
    validation_range = ws.range('B1:B5')
    validation_range.api.Validation.Add(
        Type=3,  # xlValidateList
        AlertStyle=1,  # xlValidAlertStop
        Operator=1,  # xlBetween
        Formula1="Option1,Option2,Option3"
    )
    validation_range.value = ["Option1"] * 5  # Set initial values
    print("Added data validation to range B1:B5")
    
    # AUTO-FILTERS
    # -----------
    # openpyxl:
    # ws.auto_filter.ref = 'A1:B10'
    
    # xlwings:
    ws.range('A1:B10').api.AutoFilter()
    print("Applied auto-filter to range A1:B10")
    
    wb.close()


# ===========================================================================
# 5. CHARTS AND TABLES
# ===========================================================================

def example_chart_operations():
    print("\n=== Charts and Tables ===")
    
    wb = xw.Book()
    ws = wb.sheets[0]
    
    # Prepare data for chart
    headers = [['Category', 'Value']]
    data = [
        ['A', 10],
        ['B', 20],
        ['C', 15],
        ['D', 25],
        ['E', 30]
    ]
    
    ws.range('A1').value = headers + data
    
    # CREATING A CHART
    # ---------------
    # openpyxl:
    # from openpyxl.chart import BarChart, Reference
    # chart = BarChart()
    # data_ref = Reference(ws, min_col=2, min_row=2, max_row=6)
    # cats_ref = Reference(ws, min_col=1, min_row=2, max_row=6)
    # chart.add_data(data_ref)
    # chart.set_categories(cats_ref)
    # chart.title = "Sample Bar Chart"
    # ws.add_chart(chart, "D1")
    
    # xlwings:
    chart = ws.charts.add(left=300, top=0, width=400, height=300)
    chart.chart_type = 'column'
    chart.set_source_data(ws.range('A1:B6'))
    chart.name = "SampleBarChart"
    chart.chart_title = "Sample Bar Chart"
    print("Created bar chart")
    
    # CREATING TABLES
    # --------------
    # openpyxl:
    # from openpyxl.worksheet.table import Table, TableStyleInfo
    # tab = Table(displayName="Table1", ref="A1:B6")
    # style = TableStyleInfo(name="TableStyleMedium9", showFirstColumn=False,
    #                        showLastColumn=False, showRowStripes=True, showColumnStripes=True)
    # tab.tableStyleInfo = style
    # ws.add_table(tab)
    
    # xlwings:
    table_range = ws.range('A1:B6')
    table = ws.api.ListObjects.Add(1, table_range.api.Address, None, 1)
    table.Name = "Table1"
    table.TableStyle = "TableStyleMedium9"
    print("Created Excel table")
    
    wb.close()


# ===========================================================================
# 6. WORKING WITH MULTIPLE SHEETS AND LARGE DATA
# ===========================================================================

def example_large_data_operations():
    print("\n=== Working with Large Data ===")
    
    wb = xw.Book()
    
    # COPYING SHEETS
    # -------------
    # openpyxl:
    # source = wb.active
    # target = wb.copy_worksheet(source)
    # target.title = "Sheet Copy"
    
    # xlwings:
    source = wb.sheets[0]
    source.range('A1').value = "Original Sheet"
    source.copy(name="Sheet Copy")
    print(f"Created copy of sheet: {wb.sheets['Sheet Copy'].name}")
    
    # WORKING WITH LARGE RANGES EFFICIENTLY
    # -----------------------------------
    
    print("Generating large dataset...")
    # Generate sample data (10,000 rows x 5 columns)
    large_data = np.random.rand(10000, 5)
    df_large = pd.DataFrame(large_data, columns=['A', 'B', 'C', 'D', 'E'])
    
    print("Writing large dataset to Excel...")
    # xlwings with pandas is much more efficient than openpyxl for large datasets
    target_sheet = wb.sheets.add("Large Data")
    target_sheet.range('A1').value = df_large
    
    print("Reading large dataset from Excel...")
    # Reading back large data
    df_read = target_sheet.range('A1').expand().options(pd.DataFrame, header=True).value
    print(f"Successfully processed DataFrame with shape: {df_read.shape}")
    
    # WORKING ACROSS SHEETS
    # -------------------
    summary_sheet = wb.sheets.add("Summary")
    summary_sheet.range('A1').value = [["Column", "Average", "Min", "Max"]]
    
    for i, col in enumerate(df_large.columns):
        summary_sheet.range(f'A{i+2}').value = [
            [col, 
             f'=AVERAGE(\'Large Data\'!{chr(65+i)}2:{chr(65+i)}10001)', 
             f'=MIN(\'Large Data\'!{chr(65+i)}2:{chr(65+i)}10001)',
             f'=MAX(\'Large Data\'!{chr(65+i)}2:{chr(65+i)}10001)'
            ]
        ]
    
    print("Created summary sheet with cross-sheet formulas")
    
    wb.close()


# ===========================================================================
# 7. EXCEL AUTOMATION (UNIQUE TO XLWINGS)
# ===========================================================================

def example_excel_automation():
    print("\n=== Excel Automation (Only in xlwings) ===")
    
    # WITH VISIBLE EXCEL APPLICATION
    # ----------------------------
    # Open Excel visibly (not possible with openpyxl)
    app = xw.App(visible=True)
    print("Launched visible Excel application")
    
    wb = app.books.add()
    ws = wb.sheets[0]
    ws.range('A1').value = "Excel Automation Demo"
    ws.range('A1').font.bold = True
    
    # SCREEN UPDATING CONTROL
    # ----------------------
    app.screen_updating = False
    print("Turned off screen updating for performance")
    
    # Simulate some work
    for i in range(1, 11):
        ws.range(f'A{i+2}').value = f"Row {i}"
        ws.range(f'B{i+2}').value = i * 10
    
    app.screen_updating = True
    print("Turned screen updating back on")
    
    # RUNNING EXCEL MACROS
    # ------------------
    # Note: This requires a workbook with macros (.xlsm)
    print("Example of running Excel VBA (commented out):")
    # To run a macro:
    # wb.macro("MacroName")()
    
    # ACCESSING EXCEL'S FEATURES
    # -------------------------
    # Using Excel's built-in features (spell check example)
    print("Example of using Excel's spell check API")
    ws.range('C1').value = "Mispeled word"
    
    # This would actually run spell check if uncommented:
    # misspelled = app.api.CheckSpelling(Text=ws.range('C1').value)
    # print(f"'Mispeled word' is misspelled: {misspelled}")
    
    # CLEANUP
    # -------
    app.quit()  # Close Excel application
    print("Closed Excel application")


# ===========================================================================
# 8. PUTTING IT ALL TOGETHER - A PRACTICAL EXAMPLE
# ===========================================================================

def practical_example():
    print("\n=== Practical Example: Monthly Sales Report ===")
    
    # Create sample sales data
    regions = ['North', 'South', 'East', 'West']
    products = ['Product A', 'Product B', 'Product C']
    months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun']
    
    # Generate random sales data
    np.random.seed(42)  # For reproducibility
    data = []
    for region in regions:
        for product in products:
            for month in months:
                sales = np.random.randint(1000, 10000)
                units = np.random.randint(10, 100)
                data.append([region, product, month, sales, units])
    
    # Create DataFrame
    sales_df = pd.DataFrame(data, columns=['Region', 'Product', 'Month', 'Sales', 'Units'])
    
    # Start Excel application
    app = xw.App(visible=True)
    wb = app.books.add()
    
    # 1. Create raw data sheet
    data_sheet = wb.sheets[0]
    data_sheet.name = "Sales Data"
    data_sheet.range('A1').value = sales_df
    data_sheet.range('A:E').column_width = 15
    
    # Create Excel Table for data
    table_range = data_sheet.range('A1').expand()
    table = data_sheet.api.ListObjects.Add(1, table_range.api.Address, None, 1)
    table.Name = "SalesData"
    
    # 2. Create summary sheet
    summary_sheet = wb.sheets.add("Summary")
    
    # Add title
    summary_sheet.range('A1').value = "Monthly Sales Summary"
    summary_sheet.range('A1').font.size = 16
    summary_sheet.range('A1').font.bold = True
    
    # Create pivot table
    # In openpyxl, creating pivot tables is very complex
    # In xlwings, we can use Excel's built-in functionality
    
    # Define pivot cache
    pivot_cache = wb.api.PivotCaches().Create(
        SourceType=xw.constants.PivotTableSourceType.xlDatabase,
        SourceData=f"SalesData",
        Version=xw.constants.PivotTableVersionList.xlPivotTableVersion15
    )
    
    # Create pivot table
    pivot_table = pivot_cache.CreatePivotTable(
        TableDestination=summary_sheet.range('A3').api,
        TableName="SalesPivot",
        DefaultVersion=xw.constants.PivotTableVersionList.xlPivotTableVersion15
    )
    
    # Configure pivot table fields
    pivot_table.PivotFields('Region').Orientation = xw.constants.PivotFieldOrientation.xlRowField
    pivot_table.PivotFields('Region').Position = 1
    
    pivot_table.PivotFields('Product').Orientation = xw.constants.PivotFieldOrientation.xlRowField
    pivot_table.PivotFields('Product').Position = 2
    
    pivot_table.PivotFields('Month').Orientation = xw.constants.PivotFieldOrientation.xlColumnField
    pivot_table.PivotFields('Month').Position = 1
    
    pivot_table.PivotFields('Sales').Orientation = xw.constants.PivotFieldOrientation.xlDataField
    pivot_table.PivotFields('Sales').Position = 1
    pivot_table.PivotFields('Sum of Sales').NumberFormat = "$#,##0"
    
    # 3. Create a chart sheet
    chart_sheet = wb.sheets.add("Chart")
    
    # Add title
    chart_sheet.range('A1').value = "Sales by Region"
    chart_sheet.range('A1').font.size = 16
    chart_sheet.range('A1').font.bold = True
    
    # Create pivot chart data
    region_totals = sales_df.groupby('Region')['Sales'].sum().reset_index()
    chart_sheet.range('A3').value = region_totals
    
    # Add chart
    chart = chart_sheet.charts.add(left=100, top=50, width=450, height=300)
    chart.chart_type = 'pie'
    chart.set_source_data(chart_sheet.range('A3').expand())
    chart.chart_title = "Sales by Region"
    
    # Set chart legend position
    chart.api.SetElement(msoElementLegendRight)
    
    # Add data labels
    chart.api.FullSeriesCollection(1).DataLabels.ShowPercentage = True
    chart.api.FullSeriesCollection(1).DataLabels.ShowValue = False
    
    # Format worksheet
    for sheet in wb.sheets:
        # Auto-fit columns
        sheet.autofit()
    
    # Save workbook
    wb.save('monthly_sales_report.xlsx')
    print("Created comprehensive sales report with xlwings")
    
    # Close but don't quit Excel
    wb.close()
    app.quit()


if __name__ == "__main__":
    print("OPENPYXL TO XLWINGS CONVERSION EXAMPLES")
    print("=======================================")
    
    # Run the examples
    example_workbook_operations()
    example_cell_operations()
    example_formatting_operations()
    example_formula_operations()
    example_chart_operations()
    example_large_data_operations()
    example_excel_automation()
    
    # Uncomment to run the comprehensive practical example
    # practical_example()
    
    print("\nAll examples completed!")