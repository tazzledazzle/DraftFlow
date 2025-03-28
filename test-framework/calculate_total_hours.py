import xlwings as xw
from collections import OrderedDict


def calculate_total_hours():
    wb = xw.Book("northshore_exteriors_workbook.xlsm")
    tasks = {}
    for i in wb.sheets("Job Cost Tracking").range('A:A').rows:
        if i.value is not None:
            # print(i.value)
            estimate_value = i.offset(0, 2).value
            if i.value not in tasks:
                if estimate_value.isdigit():
                    tasks[i.value] = float(estimate_value)  # get the estimate
                else:
                    tasks[i.value] = float(0)
            else:
                tasks[i.value] += float(estimate_value)
        else:
            break

    for (task) in OrderedDict(sorted(tasks.items())):
        print(f"{task}: {tasks[task]}")

        # map the values back to the excel sheet



if __name__ == "__main__":
    calculate_total_hours()