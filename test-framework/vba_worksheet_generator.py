import openpyxl
import xlwings as xw
from openpyxl.styles import Font, PatternFill, Border, Side, Alignment
from openpyxl.utils import get_column_letter

def create_northshore_workbook(filename="northshore_exteriors_workbook.xlsx"):
    """
    Create a workbook with the timesheet and daily report sheets.
    This version avoids all potential merged cell issues.
    """
    # Create a new workbook
    wb = xw.Book()

    # Set up the first sheet as TimeSheet
    ws_timesheet = wb.sheets[0]
    ws_timesheet.title = "TimeSheet"
    create_timesheet(ws_timesheet)

    # Create the daily report sheet
    ws_daily = wb.sheets(title="Sheet1")
    create_daily_report(ws_daily)

    # Create other placeholder sheets seen in the tab bar
    tab_names = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                 "Saturday", "Sunday", "Validation Table", "Job Cost Tracking"]

    for tab_name in tab_names:
        wb.sheets(title=tab_name)

    # Save workbook
    wb.save(filename)
    print( f"Workbook saved as: {filename}")
    return filename

def create_timesheet(ws):
    """Create the timesheet layout in the given worksheet."""
    # Set column widths
    for col_idx, width in enumerate([20, 18] + [12]*12 + [10, 10], 1):
        ws.column_dimensions[get_column_letter(col_idx)].width = width

    # Define styles
    (button_fill, center_align, gray_fill, green_fill, header_fill, header_font, left_align,
     normal_font, red_fill, subheader_font, thin_border) = define_styles()

    # Company header
    company_header(center_align, header_font, ws)

    # Foreman and Job info
    foreman_and_job_info(center_align, left_align, normal_font, subheader_font, thin_border, ws)

    # Week start info
    week_start_info(center_align, left_align, normal_font, subheader_font, thin_border, ws)

    # Days of the week header
    setup_days_of_the_week_headers(center_align, normal_font, subheader_font, thin_border, ws)

    # Now merge
    ws.merge_cells('N4:N5')

    # Add submit time button
    add_submit_time_button(button_fill, center_align, ws)

    # Hours Type headers
    hours_type_headers(subheader_font, ws)

    # Worked/Sick columns
    worked_sick_column_headers(center_align, subheader_font, thin_border, ws)

    # Add worker 1 section - MACKENZIE, DOUGAL
    current_row = 6

    # Payroll category header
    payroll_category_header(center_align, current_row, header_fill, normal_font, subheader_font, thin_border, ws)

    # Task rows
    tasks = create_task_rows()

    # Add task rows
    current_row = add_task_rows(center_align, current_row, normal_font, tasks, thin_border, ws)

    # Add task buttons
    button_row = 9

    add_task_buttons(button_row, center_align, normal_font, thin_border, ws)

    # Add foreman section
    current_row += 1

    current_row = add_foreman_section(current_row, normal_font, thin_border, ws)

    # Add subtotals row
    current_row += 1
    add_subtotal_rows(center_align, current_row, normal_font, thin_border, ws)

    # Add Worker 2 - BURTT, AARON
    current_row += 2

    # Payroll category header
    add_payroll_category_header(current_row, header_fill, subheader_font, ws)

    # Worker hours totals
    add_worker_hours_totals(center_align, current_row, normal_font, thin_border, ws)

    # Add worker 2 tasks
    for i in range(3):
        current_row += 1

        # Task ID
        add_task_id(center_align, current_row, normal_font, thin_border, ws)

        # Task name
        if i == 0:
            ws[f"B{current_row}"] = "Column wraps @ podium"
        else:
            ws[f"B{current_row}"] = "JOB SPECIFIC TASK"

        ws[f"B{current_row}"].font = normal_font
        ws[f"B{current_row}"].border = thin_border

    # Add worker 2 foreman section
    current_row += 1

    current_row = add_foreman_section(current_row, normal_font, thin_border, ws)

    # Add worker 2 subtotals
    current_row += 1
    add_subtotal_rows(center_align, current_row, normal_font, thin_border, ws)

    # Add Workers 3 and 4 - FULL NAME
    for worker in range(3, 5):
        current_row += 2

        # Payroll category header
        add_worker_payroll_headers(current_row, header_fill, subheader_font, ws)

        # Worker hours totals
        add_worker_hours_totals(center_align, current_row, normal_font, thin_border, ws)

        # Add tasks
        for i in range(3):
            current_row += 1

            # Task ID
            add_task_id(center_align, current_row, normal_font, thin_border, ws)

            # Task name
            add_task_name_row(current_row, normal_font, thin_border, ws)

        # Add foreman section
        current_row += 1

        current_row = add_foreman_section(current_row, normal_font, thin_border, ws)

        # Add subtotals
        current_row += 1
        add_subtotal_rows(center_align, current_row, normal_font, thin_border, ws)

    # Add summary section
    current_row += 2

    # Gray background
    fill_gray_background(current_row, gray_fill, ws)

    # Worked row
    add_worked_header(center_align, current_row, subheader_font, thin_border, ws)

    # Hours data
    current_row = fill_hours_data(center_align, current_row, normal_font, subheader_font, thin_border, ws)

    # Total sick and extra
    add_worker_hours_totals(center_align, current_row, normal_font, thin_border, ws)

    # Add/Delete Worker buttons
    current_row += 3

    # Add Worker
    create_add_worker_button(center_align, current_row, green_fill, normal_font, thin_border, ws)

    # First create and style C, D, and E cells
    style_and_merge(current_row, green_fill, thin_border, ws)

    # Delete Worker
    create_delete_worker_button(center_align, current_row, normal_font, red_fill, thin_border, ws)


def create_delete_worker_button(center_align, current_row, normal_font, red_fill, thin_border, ws):
    cell = ws.Cell(f"Q{current_row}")
    cell.value = "Delete Worker"
    cell.font = normal_font
    cell.border = thin_border
    cell.alignment = center_align
    cell.fill = red_fill


def style_and_merge(current_row, green_fill, thin_border, ws):
    for col_letter in ['C', 'D', 'E']:
        cell = ws.Cell(f"{col_letter}{current_row}")
        cell.fill = green_fill
        cell.border = thin_border
    # Then merge
    ws.merge_cells(f'C{current_row}:E{current_row}')


def create_add_worker_button(center_align, current_row, green_fill, normal_font, thin_border, ws):
    cell = ws.Cell(f"C{current_row}")
    cell.value = "Add Worker +"
    cell.font = normal_font
    cell.border = thin_border
    cell.alignment = center_align
    cell.fill = green_fill


def fill_hours_data(center_align, current_row, normal_font, subheader_font, thin_border, ws):
    hours_data = [5, 4, 8, 1, 0, 0, 0]
    hours_columns = ['C', 'E', 'G', 'I', 'K', 'M', 'N']
    for idx, (col, hours) in enumerate(zip(hours_columns, hours_data)):
        cell = ws.Cell(f"{col}{current_row}")
        cell.value = hours
        cell.font = normal_font
        cell.border = thin_border
        cell.alignment = center_align
    # Total
    ws_cell = ws.Cell(f"O{current_row}")
    ws_cell.value = 18  # Total shown in the image
    ws_cell.font = subheader_font
    ws_cell.border = thin_border
    ws_cell.alignment = center_align
    # Sick row
    current_row += 1
    ws[f"B{current_row}"] = "Sick"
    ws[f"B{current_row}"].font = subheader_font
    ws[f"B{current_row}"].border = thin_border
    ws[f"B{current_row}"].alignment = center_align
    # Zero sick hours
    for col in hours_columns:
        ws[f"{col}{current_row}"] = 0
        ws[f"{col}{current_row}"].font = normal_font
        ws[f"{col}{current_row}"].border = thin_border
        ws[f"{col}{current_row}"].alignment = center_align
    return current_row


def fill_gray_background(current_row, gray_fill, ws):
    for row in range(current_row, current_row + 3):
        for col in range(1, 17):
            cell = ws.cell(row=row, column=col)
            cell.fill = gray_fill


def add_worked_header(center_align, current_row, subheader_font, thin_border, ws):
    ws[f"B{current_row}"] = "Worked"
    ws[f"B{current_row}"].font = subheader_font
    ws[f"B{current_row}"].border = thin_border
    ws[f"B{current_row}"].alignment = center_align


def add_task_name_row(current_row, normal_font, thin_border, ws):
    ws[f"B{current_row}"] = "JOB SPECIFIC TASK"
    ws[f"B{current_row}"].font = normal_font
    ws[f"B{current_row}"].border = thin_border


def add_worker_payroll_headers(current_row, header_fill, subheader_font, ws):
    ws[f"A{current_row}"] = "PAYROLL CATEGORY"
    ws[f"A{current_row}"].font = subheader_font
    ws[f"A{current_row}"].fill = header_fill
    ws[f"B{current_row}"] = "FULL NAME"
    ws[f"B{current_row}"].font = subheader_font
    ws[f"B{current_row}"].fill = header_fill


def add_task_id(center_align, current_row, normal_font, thin_border, ws):
    ws[f"A{current_row}"] = 1
    ws[f"A{current_row}"].font = normal_font
    ws[f"A{current_row}"].border = thin_border
    ws[f"A{current_row}"].alignment = center_align


def add_subtotal_rows(center_align, current_row, normal_font, thin_border, ws):
    for col_idx in range(3, 15):
        col_letter = get_column_letter(col_idx)
        ws[f"{col_letter}{current_row}"] = 0
        ws[f"{col_letter}{current_row}"].font = normal_font
        ws[f"{col_letter}{current_row}"].border = thin_border
        ws[f"{col_letter}{current_row}"].alignment = center_align


def add_worker_hours_totals(center_align, current_row, normal_font, thin_border, ws):
    ws[f"O{current_row}"] = 0
    ws[f"O{current_row}"].font = normal_font
    ws[f"O{current_row}"].border = thin_border
    ws[f"O{current_row}"].alignment = center_align
    ws[f"P{current_row}"] = 0
    ws[f"P{current_row}"].font = normal_font
    ws[f"P{current_row}"].border = thin_border
    ws[f"P{current_row}"].alignment = center_align


def add_payroll_category_header(current_row, header_fill, subheader_font, ws):
    ws[f"A{current_row}"] = "PAYROLL CATEGORY"
    ws[f"A{current_row}"].font = subheader_font
    ws[f"A{current_row}"].fill = header_fill
    ws[f"B{current_row}"] = "BURTT, AARON"
    ws[f"B{current_row}"].font = subheader_font
    ws[f"B{current_row}"].fill = header_fill


def add_foreman_section(current_row, normal_font, thin_border, ws):
    ws[f"B{current_row}"] = "Acting Foreman"
    ws[f"B{current_row}"].font = normal_font
    ws[f"B{current_row}"].border = thin_border
    current_row += 1
    ws[f"B{current_row}"] = "Sick Hours Requested"
    ws[f"B{current_row}"].font = normal_font
    ws[f"B{current_row}"].border = thin_border
    current_row += 1
    ws[f"B{current_row}"] = "Reason Off if not sick"
    ws[f"B{current_row}"].font = normal_font
    ws[f"B{current_row}"].border = thin_border
    return current_row


def add_task_buttons(button_row, center_align, normal_font, thin_border, ws):
    ws[f"Q{button_row}"] = "Add Task Row"
    ws[f"Q{button_row}"].font = normal_font
    ws[f"Q{button_row}"].border = thin_border
    ws[f"Q{button_row}"].alignment = center_align
    ws[f"Q{button_row}"].fill = PatternFill(start_color='DDDDDD', end_color='DDDDDD', fill_type='solid')
    ws[f"Q{button_row + 1}"] = "Delete Task Row"
    ws[f"Q{button_row + 1}"].font = normal_font
    ws[f"Q{button_row + 1}"].border = thin_border
    ws[f"Q{button_row + 1}"].alignment = center_align
    ws[f"Q{button_row + 1}"].fill = PatternFill(start_color='DDDDDD', end_color='DDDDDD', fill_type='solid')


def add_task_rows(center_align, current_row, normal_font, tasks, thin_border, ws):
    for task in tasks:
        current_row += 1

        # Task ID
        ws[f"A{current_row}"] = task["id"]
        ws[f"A{current_row}"].font = normal_font
        ws[f"A{current_row}"].border = thin_border
        ws[f"A{current_row}"].alignment = center_align

        # Task name
        ws[f"B{current_row}"] = task["name"]
        ws[f"B{current_row}"].font = normal_font
        ws[f"B{current_row}"].border = thin_border

        # Add hours
        if "monday_reg" in task["hours"]:
            ws[f"C{current_row}"] = task["hours"]["monday_reg"]
            ws[f"C{current_row}"].border = thin_border
            ws[f"C{current_row}"].alignment = center_align

        if "tuesday_reg" in task["hours"]:
            ws[f"E{current_row}"] = task["hours"]["tuesday_reg"]
            ws[f"E{current_row}"].border = thin_border
            ws[f"E{current_row}"].alignment = center_align

        if "wednesday_reg" in task["hours"]:
            ws[f"G{current_row}"] = task["hours"]["wednesday_reg"]
            ws[f"G{current_row}"].border = thin_border
            ws[f"G{current_row}"].alignment = center_align

        if "thursday_reg" in task["hours"]:
            ws[f"I{current_row}"] = task["hours"]["thursday_reg"]
            ws[f"I{current_row}"].border = thin_border
            ws[f"I{current_row}"].alignment = center_align
    return current_row


def create_task_rows():
    tasks = [
        {"id": 1, "name": "Penthouse siding and coping",
         "hours": {"monday_reg": 5, "tuesday_reg": 3, "wednesday_reg": 8, "thursday_reg": 1}},
        {"id": 2, "name": "Sheet metal roof @ L14 canopy", "hours": {"tuesday_reg": 1}},
        {"id": 3, "name": "Sheet metal roof @ L14 canopy (roof build up)", "hours": {}}
    ]
    return tasks


def payroll_category_header(center_align, current_row, header_fill, normal_font, subheader_font, thin_border, ws):
    ws[f"A{current_row}"] = "PAYROLL CATEGORY"
    ws[f"A{current_row}"].font = subheader_font
    ws[f"A{current_row}"].fill = header_fill
    ws[f"B{current_row}"] = "MACKENZIE, DOUGAL"
    ws[f"B{current_row}"].font = subheader_font
    ws[f"B{current_row}"].fill = header_fill
    # Worker hours totals
    ws[f"O{current_row}"] = 17
    ws[f"O{current_row}"].font = normal_font
    ws[f"O{current_row}"].border = thin_border
    ws[f"O{current_row}"].alignment = center_align
    ws[f"P{current_row}"] = 0
    ws[f"P{current_row}"].font = normal_font
    ws[f"P{current_row}"].border = thin_border
    ws[f"P{current_row}"].alignment = center_align


def hours_type_headers(subheader_font, ws):
    ws['A5'] = "Hours Type"
    ws['A5'].font = subheader_font
    ws['B5'] = "Hours Type"
    ws['B5'].font = subheader_font


def worked_sick_column_headers(center_align, subheader_font, thin_border, ws):
    ws['O4'] = "Worked"
    ws['O4'].font = subheader_font
    ws['O4'].border = thin_border
    ws['O4'].alignment = center_align
    ws['P4'] = "Sick"
    ws['P4'].font = subheader_font
    ws['P4'].border = thin_border
    ws['P4'].alignment = center_align


def add_submit_time_button(button_fill, center_align, ws):
    cell = ws.cell(row=1, column=10 )#, value="Submit Time")
    # cell.value = "Submit Time" # this cannot happen because value is read-only
    cell.font = Font(bold=True, color="FFFFFF")
    cell.fill = button_fill
    cell.alignment = center_align
    ws.merge_cells(range_string='J1:K1')


def set_ot_value(center_align, normal_font, thin_border, ws):
    # We need a different approach for merged cells
    # Create and style the cell first, with value
    cell = ws.cell(row=5, column=13)  # M5
    cell.value = "OT"
    cell.font = normal_font
    cell.border = thin_border
    cell.alignment = center_align
    # Now merge - the value will be retained in the top-left cell (M4)
    ws.merge_cells('M4:M5')


def setup_days_of_the_week_headers(center_align, normal_font, subheader_font, thin_border, ws):
    days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    # Monday through Friday (columns C-L)
    for i in range(5):  # 0-4 for Monday-Friday
        day_col = chr(ord('C') + i * 2)  # C, E, G, I, K

        # Set day name
        ws[f"{day_col}4"] = days[i]
        ws[f"{day_col}4"].font = subheader_font
        ws[f"{day_col}4"].border = thin_border
        ws[f"{day_col}4"].alignment = center_align

        # REG column
        ws[f"{day_col}5"] = "REG"
        ws[f"{day_col}5"].font = normal_font
        ws[f"{day_col}5"].border = thin_border
        ws[f"{day_col}5"].alignment = center_align

        # OT column
        ot_col = chr(ord(day_col) + 1)  # D, F, H, J, L
        ws[f"{ot_col}5"] = "OT"
        ws[f"{ot_col}5"].font = normal_font
        ws[f"{ot_col}5"].border = thin_border
        ws[f"{ot_col}5"].alignment = center_align
    # Saturday (column M) - set values BEFORE merging
    ws['M4'] = "Saturday"
    ws['M4'].font = subheader_font
    ws['M4'].border = thin_border
    ws['M4'].alignment = center_align

    # Set OT value
    set_ot_value(center_align, normal_font, thin_border, ws)

    # Sunday (column N) - set values BEFORE merging
    ws['N4'] = "Sunday"
    ws['N4'].font = subheader_font
    ws['N4'].border = thin_border
    ws['N4'].alignment = center_align

    # Set DT value
    cell = ws.cell(row=5, column=14)  # N5
    cell.value = "DT"
    cell.font = normal_font
    cell.border = thin_border
    cell.alignment = center_align


def week_start_info(center_align, left_align, normal_font, subheader_font, thin_border, ws):
    ws['A3'] = "Week Start Monday:"
    ws['A3'].font = subheader_font
    ws['A3'].alignment = left_align
    ws['C3'] = "3/24/25"
    ws['C3'].font = normal_font
    ws['C3'].border = thin_border
    ws['C3'].alignment = center_align
    ws['E3'] = "Project"
    ws['E3'].font = subheader_font
    ws['E3'].alignment = center_align
    ws['F3'] = 0
    ws['F3'].font = normal_font
    ws['F3'].border = thin_border
    ws['F3'].alignment = center_align
    ws['I3'] = "GC"
    ws['I3'].font = subheader_font
    ws['I3'].alignment = center_align


def foreman_and_job_info(center_align, left_align, normal_font, subheader_font, thin_border, ws):
    ws['A2'] = "Foreman:"
    ws['A2'].font = subheader_font
    ws['A2'].alignment = left_align
    ws['C2'] = "GREIN, CHRISTOPHER"
    ws['C2'].font = normal_font
    ws['C2'].border = thin_border
    ws.merge_cells('C2:D2')
    ws['E2'] = "Job #"
    ws['E2'].font = subheader_font
    ws['E2'].alignment = center_align
    ws['F2'] = 0
    ws['F2'].font = normal_font
    ws['F2'].border = thin_border
    ws['F2'].alignment = center_align
    ws['I2'] = "Shift Type"
    ws['I2'].font = subheader_font
    ws['I2'].alignment = center_align
    ws['M2'] = "Day_S-8"
    ws['M2'].font = normal_font
    ws['M2'].alignment = center_align
    ws['O2'] = "FALSE"
    ws['O2'].font = normal_font
    ws['O2'].alignment = center_align


def company_header(center_align, header_font, ws):
    ws['A1'] = "NORTHSHORE EXTERIORS"
    ws['A1'].font = header_font
    ws.merge_cells('A1:O1')
    ws['A1'].alignment = center_align


def define_styles():
    thin_border = Border(
        left=Side(style='thin'),
        right=Side(style='thin'),
        top=Side(style='thin'),
        bottom=Side(style='thin')
    )

    header_font = Font(name='Arial', size=14, bold=True, color='000080')
    subheader_font = Font(name='Arial', size=10, bold=True)
    normal_font = Font(name='Arial', size=10)
    white_font = Font(name='Arial', size=10, bold=True, color='FFFFFF')

    center_align = Alignment(horizontal='center', vertical='center')
    left_align = Alignment(horizontal='left', vertical='center')

    header_fill = PatternFill(start_color='D0D0D0', end_color='D0D0D0', fill_type='solid')
    button_fill = PatternFill(start_color='4472C4', end_color='4472C4', fill_type='solid')
    green_fill = PatternFill(start_color='92D050', end_color='92D050', fill_type='solid')
    red_fill = PatternFill(start_color='FF0000', end_color='FF0000', fill_type='solid')
    gray_fill = PatternFill(start_color='BBBBBB', end_color='BBBBBB', fill_type='solid')
    return button_fill, center_align, gray_fill, green_fill, header_fill, header_font, left_align, normal_font, red_fill, subheader_font, thin_border


def create_daily_report(ws):
    """Create a simplified daily report layout."""
    # Set column widths
    for col_idx in range(1, 15):
        ws.column_dimensions[get_column_letter(col_idx)].width = 15

    # Define styles
    thin_border = Border(
        left=Side(style='thin'),
        right=Side(style='thin'),
        top=Side(style='thin'),
        bottom=Side(style='thin')
    )

    header_font = Font(name='Arial', size=12, bold=True)
    subheader_font = Font(name='Arial', size=10, bold=True)
    normal_font = Font(name='Arial', size=10)
    white_font = Font(name='Arial', size=10, bold=True, color='FFFFFF')

    center_align = Alignment(horizontal='center', vertical='center')
    left_align = Alignment(horizontal='left', vertical='center')
    right_align = Alignment(horizontal='right', vertical='center')

    blue_fill = PatternFill(start_color='4472C4', end_color='4472C4', fill_type='solid')
    black_fill = PatternFill(start_color='000000', end_color='000000', fill_type='solid')

    # Create blue background for logo area
    for row in range(1, 8):
        ws.cell(row=row, column=1).fill = blue_fill

    # Company info
    ws.cell(row=2, column=2).value = "Northshore Exteriors, Inc."
    ws.cell(row=2, column=2).font = header_font
    ws.cell(row=2, column=2).alignment = left_align

    ws.cell(row=3, column=2).value = "11831 Beverly Park Rd, Building C"
    ws.cell(row=3, column=2).font = normal_font
    ws.cell(row=3, column=2).alignment = left_align

    ws.cell(row=4, column=2).value = "Everett WA 98204"
    ws.cell(row=4, column=2).font = normal_font
    ws.cell(row=4, column=2).alignment = left_align

    # Report title
    ws.cell(row=2, column=5).value = "Daily Report"
    ws.cell(row=2, column=5).font = Font(name='Arial', size=16, bold=True)
    ws.cell(row=2, column=5).alignment = center_align

    # Prepare cells for merging
    for col in range(6, 10):
        ws.cell(row=2, column=col).value = None
    ws.merge_cells('E2:I2')

    # Create Daily PDF button
    ws.cell(row=2, column=12).value = "Create Daily Pdf"
    ws.cell(row=2, column=12).font = white_font
    ws.cell(row=2, column=12).alignment = center_align
    ws.cell(row=2, column=12).fill = blue_fill

    # Prepare for merging
    ws.cell(row=2, column=13).value = None
    ws.cell(row=2, column=13).fill = blue_fill
    ws.merge_cells('L2:M2')

    # Fill This Daily button
    ws.cell(row=4, column=12).value = "Fill This Daily"
    ws.cell(row=4, column=12).font = white_font
    ws.cell(row=4, column=12).alignment = center_align
    ws.cell(row=4, column=12).fill = black_fill

    # Prepare for merging
    ws.cell(row=4, column=13).value = None
    ws.cell(row=4, column=13).fill = black_fill
    ws.merge_cells('L4:M4')

    # Employees on Site section
    ws.cell(row=9, column=3).value = "Employees on Site"
    ws.cell(row=9, column=3).font = subheader_font
    ws.cell(row=9, column=3).alignment = center_align

    # Prepare for merging
    for col in range(4, 10):
        ws.cell(row=9, column=col).value = None
    ws.merge_cells('C9:I9')

    # Headers for employee table
    headers = ["Name", "Position Level", "Hrs", "Area of Site Working / Work Completed (sq/ft)"]
    col_positions = [2, 3, 4, 5]  # B, C, D, E

    for idx, header in enumerate(headers):
        col = col_positions[idx]
        cell = ws.cell(row=10, column=col)
        cell.value = header
        cell.font = normal_font
        cell.alignment = center_align
        cell.border = thin_border

    # Prepare for merging the last header
    for col in range(6, 10):
        ws.cell(row=10, column=col).value = None
        ws.cell(row=10, column=col).border = thin_border
    ws.merge_cells('E10:I10')

    # Create employee rows with borders
    for row in range(11, 26):
        for col in range(2, 10):
            ws.cell(row=row, column=col).border = thin_border

    # Add vertical area labels
    # On Wall - prepare for vertical text
    ws.cell(row=11, column=5).value = "On Wall"
    ws.cell(row=11, column=5).alignment = Alignment(textRotation=90, horizontal='center', vertical='center')

    # Prepare cells for merging
    for row in range(12, 16):
        ws.cell(row=row, column=5).value = None
        ws.cell(row=row, column=5).border = thin_border
    ws.merge_cells('E11:E15')

    # On Roof
    ws.cell(row=16, column=5).value = "On Roof"
    ws.cell(row=16, column=5).alignment = Alignment(textRotation=90, horizontal='center', vertical='center')

    # Prepare cells for merging
    for row in range(17, 21):
        ws.cell(row=row, column=5).value = None
        ws.cell(row=row, column=5).border = thin_border
    ws.merge_cells('E16:E20')

    # Other
    ws.cell(row=21, column=5).value = "Other"
    ws.cell(row=21, column=5).alignment = Alignment(textRotation=90, horizontal='center', vertical='center')

    # Prepare cells for merging
    for row in range(22, 26):
        ws.cell(row=row, column=5).value = None
        ws.cell(row=row, column=5).border = thin_border
    ws.merge_cells('E21:E25')

if __name__ == "__main__":
    file_path = create_northshore_workbook()
    print(f"Created file: {file_path}")