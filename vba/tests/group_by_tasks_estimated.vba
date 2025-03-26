Sub GroupTasksAndSummarize()
    Dim sourceBook As Workbook
    Dim sourceSheet As Worksheet
    Dim targetBook As Workbook
    Dim jobTrackSheet As Worksheet
    Dim cell As Range
    Dim hoursCell As Range
    Dim estCell As Range
    Dim projectID As String
    Dim numTaskID As Integer
    Dim i As Integer
    Dim lastRow As Long

    ' Create a 2D array to store task data (1=Name, 2=ID, 3=Hours, 4=Estimate)
    Dim taskArray() As Variant
    ReDim taskArray(1 To 500, 1 To 4)
    Dim taskCount As Integer
    taskCount = 0

    ' Open the source workbook - modify path as needed
    Set sourceBook = Workbooks.Open(ThisWorkbook.Path & "\..\tests\Test6446Job.xlsx")
    Set sourceSheet = sourceBook.Worksheets("Sheet1")

    ' Get project ID from cell F2
    projectID = sourceSheet.Range("F2").Value

    ' Initialize task counter
    numTaskID = 1

    ' Find last row with data in column B
    lastRow = sourceSheet.Cells(sourceSheet.Rows.Count, "B").End(xlUp).Row

    ' Loop through each row
    For i = 2 To lastRow
        ' Get cell values
        Set cell = sourceSheet.Range("B" & i)
        Set hoursCell = sourceSheet.Range("C" & i)
        Set estCell = sourceSheet.Range("K" & i)

        ' Optional: set task ID in column L
        ' sourceSheet.Range("L" & i).Value = projectID & "-" & numTaskID
        numTaskID = numTaskID + 1

        ' Skip empty cells
        If Not IsEmpty(cell.Value) Then
            ' Check if task already exists in our array
            Dim found As Boolean
            Dim j As Integer
            found = False

            For j = 1 To taskCount
                If taskArray(j, 1) = cell.Value Then
                    ' Task exists, update hours and estimate
                    If Not IsEmpty(hoursCell.Value) Then
                        taskArray(j, 3) = taskArray(j, 3) + hoursCell.Value
                    End If

                    If Not IsEmpty(estCell.Value) Then
                        taskArray(j, 4) = taskArray(j, 4) + estCell.Value
                    End If

                    found = True
                    Exit For
                End If
            Next j

            ' If task not found, add it
            If Not found Then
                taskCount = taskCount + 1

                ' Resize array if needed
                If taskCount > UBound(taskArray, 1) Then
                    ReDim Preserve taskArray(1 To UBound(taskArray, 1) + 500, 1 To 4)
                End If

                ' Store task data
                taskArray(taskCount, 1) = cell.Value                             ' Task name
                taskArray(taskCount, 2) = projectID & "-" & numTaskID            ' Task ID
                taskArray(taskCount, 3) = IIf(IsEmpty(hoursCell.Value), 0, hoursCell.Value) ' Hours
                taskArray(taskCount, 4) = IIf(IsEmpty(estCell.Value), 0, estCell.Value)     ' Estimate
            End If
        End If
    Next i

    ' Open target workbook or create if not existing - modify path as needed
    On Error Resume Next
    Set targetBook = Workbooks.Open(ThisWorkbook.Path & "\..\tests\TestXLWingsSheet.xlsm")

    If Err.Number <> 0 Then
        ' Create a new workbook and add the sheet
        Set targetBook = Workbooks.Add
        With targetBook
            .SaveAs ThisWorkbook.Path & "\..\tests\TestXLWingsSheet.xlsm", xlOpenXMLWorkbookMacroEnabled
        End With
    End If
    On Error GoTo 0

    ' Check if sheet exists, if not create it
    On Error Resume Next
    Set jobTrackSheet = targetBook.Sheets("Job Cost Tracking")
    If jobTrackSheet Is Nothing Then
        ' Add sheet
        Set jobTrackSheet = targetBook.Sheets.Add
        jobTrackSheet.Name = "Job Cost Tracking"

        ' Set up headers
        jobTrackSheet.Range("A1").Value = "Task Name"
        jobTrackSheet.Range("B1").Value = "Task ID"
        jobTrackSheet.Range("C1").Value = "Estimate"
        jobTrackSheet.Range("D1").Value = "Hours"
    End If
    On Error GoTo 0

    ' Output to Job Cost Tracking sheet
    For i = 1 To taskCount
        jobTrackSheet.Range("A" & i + 1).Value = taskArray(i, 1)    ' Task name
        jobTrackSheet.Range("B" & i + 1).Value = taskArray(i, 2)    ' Task ID
        jobTrackSheet.Range("C" & i + 1).Value = taskArray(i, 4)    ' Estimate
        jobTrackSheet.Range("D" & i + 1).Value = taskArray(i, 3)    ' Hours
    Next i

    ' Format as a table for better readability
    Dim tableRange As Range
    Set tableRange = jobTrackSheet.Range("A1").Resize(taskCount + 1, 4)
    jobTrackSheet.ListObjects.Add(xlSrcRange, tableRange, , xlYes).Name = "TaskSummary"

    ' Save and close the source book
    sourceBook.Save
    sourceBook.Close

    ' Save the target book
    targetBook.Save

    MsgBox "Task grouping complete! Results saved to " & targetBook.Name, vbInformation
End Sub