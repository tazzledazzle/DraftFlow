import pandas as pd
import datetime as dt
import xlwings as xw
import numpy as np


random = pd.DataFrame(data=np.random.randn(100, 5), columns=[f"Trial {i}" for i in range(1, 6)])
# df = pd.read_excel("tests/Test-Lists-Stephanie 5.21.24 version.xlsm")
print(random)

# xw.view(random)

vba_book = xw.Book("tests/TestXLWingsSheet.xlsm")

test_wing = vba_book.macro("Module8.TestXlWing")

print(test_wing())