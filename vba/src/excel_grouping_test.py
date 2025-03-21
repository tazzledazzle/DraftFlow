import xlwings

from vba.src.excel_vba_test_case import ExcelVBATestCase


class GroupingTest(ExcelVBATestCase):
    excel_file_path = r"../tests/Test6446Job.xlsx"

    def test_grouping(self):
        # Test grouping functionality

        book = xlwings.Book("../tests/Test6446Job.xlsx")
        out_book = xlwings.Book("../tests/TestXLWingsSheet.xlsm")
        job_track_sheet = out_book.sheets("Job Cost Tracking")
        sheet = book.sheets("Sheet1")
        task_groups = {}

        # loop through the cells in the range B2:B1500
        # L is task Id that needs to be generated from projectId and taskId
        for cell, hrs, est in zip(
            sheet.range("B2:B1500"), sheet.range("C2:C1500"), sheet.range("K2:K1500")
        ):
            if cell.value is not None:
                print(cell.value)
                if cell.value not in task_groups:
                    task_groups[cell.value] = hrs.value
                else:
                    task_groups[cell.value] += (
                        hrs.value if hrs.value is not None else 0.0
                    )

        print(task_groups)
        new_sheet = book.sheets.add("Task Groups")
        book.save()
        new_sheet.range("A1").value = "Task Group"
        new_sheet.range("B1").value = "Hours"
        i = 2
        for key, value in zip(list(task_groups.keys()), list(task_groups.values())):
            new_sheet.range(f"A{i}").value = key
            new_sheet.range(f"B{i}").value = value
            i += 1

        book.save()
        print("created new sheet")
        book.close()

        pass
