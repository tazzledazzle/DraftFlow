import xlwings

from vba.src.excel_vba_test_case import ExcelVBATestCase


class GroupingTest(ExcelVBATestCase):
    excel_file_path = r"../tests/Test6446Job.xlsx"

    def test_grouping(self):
        # Test grouping functionality

        book = xlwings.Book("../tests/Test6446Job.xlsx")
        sheet = book.sheets("Sheet1")
        task_groups = {}
        # since this is being generated from test data might be a later todo
        project_id = sheet.range("F2").value

        # loop through the cells in the range B2:B1500
        # L is task Id that needs to be generated from projectId and taskId
        num_task_id = 1
        for cell, hrs, est in zip(sheet.range("B2:B1500"), sheet.range("C2:C1500"), sheet.range("K2:K1500")):
            # sheet.range(f"L{cell.row}").value = f"{project_id}-{num_task_id}"
            num_task_id += 1
            print(cell.value)
            if cell.value is not None:
                if cell.value not in task_groups:
                    task_groups[cell.value] = TaskInfo(task_id=f"{project_id}-{num_task_id}",
                                                   task_name=cell.value,
                                                   task_hours=hrs.value if hrs.value is not None else 0.0,
                                                   task_estimate=est.value if est.value is not None else 0.0
                                                   )
                else:
                    task_groups[cell.value].task_hours += (
                        hrs.value if hrs.value is not None else 0.0
                    )
                    task_groups[cell.value].task_estimate += (
                        est.value if est.value is not None else 0.0
                    )

        print(task_groups)
        out_book = xlwings.Book("../tests/TestXLWingsSheet.xlsm")
        job_track_sheet = out_book.sheets("Job Cost Tracking")
        # new_sheet = book.sheets.add("Task Groups")
        # book.save()
        # new_sheet.range("A1").value = "Task Group"
        # new_sheet.range("B1").value = "Hours"
        # new_sheet.range("C1").value = "Estimate"
        # new_sheet.range("D1").value = "TaskId"

        i = 2
        for key, task in task_groups.items():
            job_track_sheet.range(f"A{i}").value = task.task_name
            job_track_sheet.range(f"C{i}").value = task.task_estimate
            job_track_sheet.range(f"D{i}").value = task.task_hours
            i += 1

        book.save()
        print("created new sheet")
        book.close()

        pass


class TaskInfo:
    def __init__(self, task_id, task_name, task_hours, task_estimate):
        self.task_id = task_id
        self.task_name = task_name
        self.task_hours = task_hours
        self.task_estimate = task_estimate
