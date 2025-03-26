' Sample VBA module for testing
Option Explicit

' Sums values in a column range
Public Sub SumValues()
    ' Sum values in range A1:A3 and put result in A4
    Range("A4").Value = WorksheetFunction.Sum(Range("A1:A3"))
End Sub

' Multiply two cell values
Public Function MultiplyValues(cell1 As String, cell2 As String) As Double
    ' Multiply values in two cells
    MultiplyValues = Range(cell1).Value * Range(cell2).Value
End Function

' Create a formula in a specified row
Public Sub CreateFormula(rowNum As Integer)
    ' Create a formula that adds cells in columns A and B
    Cells(rowNum, 3).Formula = "=A" & rowNum & "+B" & rowNum
End Sub

' Format cell range as currency
Public Sub FormatAsCurrency(startCell As String, endCell As String)
    ' Format cell range as currency
    Range(startCell & ":" & endCell).NumberFormat = "$#,##0.00"
End Sub

' Calculate average of a range
Public Function CalculateAverage(rangeAddress As String) As Double
    ' Calculate average of a range
    CalculateAverage = WorksheetFunction.Average(Range(rangeAddress))
End Function

' Create a simple data table
Public Sub CreateDataTable()
    ' Clear target range
    Range("A10:D15").ClearContents
    
    ' Add headers
    Range("A10").Value = "ID"
    Range("B10").Value = "Name"
    Range("C10").Value = "Quantity"
    Range("D10").Value = "Price"
    
    ' Add sample data
    Range("A11").Value = 1
    Range("B11").Value = "Product A"
    Range("C11").Value = 5
    Range("D11").Value = 10.99
    
    Range("A12").Value = 2
    Range("B12").Value = "Product B"
    Range("C12").Value = 3
    Range("D12").Value = 25.5
    
    Range("A13").Value = 3
    Range("B13").Value = "Product C"
    Range("C13").Value = 8
    Range("D13").Value = 5.75
    
    Range("A14").Value = 4
    Range("B14").Value = "Product D"
    Range("C14").Value = 2
    Range("D14").Value = 48.99
    
    ' Format header row
    Range("A10:D10").Font.Bold = True
    
    ' Format price column
    Range("D11:D14").NumberFormat = "$#,##0.00"
    
    ' Add total row
    Range("A15").Value = "Total"
    Range("A15").Font.Bold = True
    Range("C15").Formula = "=SUM(C11:C14)"
    Range("D15").Formula = "=SUM(D11:D14)"
    Range("D15").NumberFormat = "$#,##0.00"
End Sub

' Calculate total value (quantity * price)
Public Sub CalculateTotalValues()
    ' Add Total Value column header
    Range("E10").Value = "Total Value"
    Range("E10").Font.Bold = True
    
    ' Calculate total value for each product
    Dim lastRow As Integer
    lastRow = WorksheetFunction.CountA(Range("A:A"))
    
    Dim i As Integer
    For i = 11 To lastRow - 1
        Range("E" & i).Formula = "=C" & i & "*D" & i
        Range("E" & i).NumberFormat = "$#,##0.00"
    Next i
    
    ' Add total sum
    Range("E" & lastRow).Formula = "=SUM(E11:E" & (lastRow - 1) & ")"
    Range("E" & lastRow).NumberFormat = "$#,##0.00"
End Sub

' Find product by name and return its price
Public Function FindProductPrice(productName As String) As Variant
    Dim lastRow As Integer
    lastRow = WorksheetFunction.CountA(Range("A:A"))
    
    Dim i As Integer
    For i = 11 To lastRow - 1
        If Range("B" & i).Value = productName Then
            FindProductPrice = Range("D" & i).Value
            Exit Function
        End If
    Next i
    
    ' Product not found
    FindProductPrice = CVErr(xlErrNA)
End Function

' Apply conditional formatting to quantity column
Public Sub ApplyConditionalFormatting()
    Dim lastRow As Integer
    lastRow = WorksheetFunction.CountA(Range("A:A"))
    
    ' Clear any existing conditional formatting
    Range("C11:C" & (lastRow - 1)).FormatConditions.Delete
    
    ' Add conditional formatting for low stock (less than 5)
    With Range("C11:C" & (lastRow - 1)).FormatConditions.Add(Type:=xlCellValue, Operator:=xlLess, Formula1:="5")
        .Interior.Color = RGB(255, 200, 200)  ' Light red
        .StopIfTrue = False
    End With
    
    ' Add conditional formatting for high stock (more than 10)
    With Range("C11:C" & (lastRow - 1)).FormatConditions.Add(Type:=xlCellValue, Operator:=xlGreater, Formula1:="10")
        .Interior.Color = RGB(200, 255, 200)  ' Light green
        .StopIfTrue = False
    End With
End Sub