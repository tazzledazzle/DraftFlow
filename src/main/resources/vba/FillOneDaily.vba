
Sub FillOneDaily() 'see fill all dailyes
    ' Variable declarations with proper types
    Dim Msg As String
    Dim Style As Integer
    Dim Title As String
    Dim Help As String
    Dim Ctxt As Integer
    Dim Response As Integer
    
    ' Error tracking variables
    Dim errCount As Integer
    Dim hasErrors As Boolean
    
    ' Initialize error tracking
    errCount = 0
    hasErrors = False
    
    Msg = "FillOneDaily: Confirm you would like to Fill this Daily Report. Some Data in 'Area of Site Work' section may be deleted."
    Style = vbYesNo Or vbInformation Or vbDefaultButton2
    Title = "Submission form"    ' Define title.
    Help = "DEMO.HLP"    ' Define Help file.
    Ctxt = 1000    ' Define topic context.
            ' Display message.
    Response = MsgBox(Msg, Style, Title, Help, Ctxt)
    
    Dim WorkDailyFilename As String
    Dim TimeCardBook As Workbook
    Dim DailyWorkbook As Workbook
    
    ' Explicitly declare objects and validate they exist
    On Error Resume Next
    ' Use ThisWorkbook rather than ActiveWorkbook for more reliability
    Set TimeCardBook = Application.ThisWorkbook
    On Error GoTo 0
    
    If TimeCardBook Is Nothing Then
        MsgBox "Could not access the workbook. Please try again.", vbExclamation
        Exit Sub
    End If
        
    Dim TimeCard As Worksheet
    On Error Resume Next
    Set TimeCard = TimeCardBook.Worksheets("TimeSheet")
    On Error GoTo 0
    
    If TimeCard Is Nothing Then
        MsgBox "The 'TimeSheet' worksheet was not found in the workbook.", vbExclamation
        Exit Sub
    End If
    
    Dim Daily As Worksheet
    On Error Resume Next
    Set Daily = TimeCardBook.ActiveSheet
    On Error GoTo 0
    
    If Daily Is Nothing Then
        MsgBox "No active worksheet found.", vbExclamation
        Exit Sub
    End If
    
    If Response = vbYes Then    ' User chose Yes.
        ' Validate date in cell C3 is a Monday
        Dim dateValue As Variant
        On Error Resume Next
        dateValue = TimeCard.Cells(3, 3).Value
        On Error GoTo 0
        
        If Not IsDate(dateValue) Then
            MsgBox "Cell C3 does not contain a valid date.", vbExclamation
            Exit Sub
        End If

        If Application.Weekday(dateValue, 2) = 1 Then
       
            ' Validate Validation Table exists
            Dim VT As Worksheet
            On Error Resume Next
            Set VT = TimeCardBook.Worksheets("Validation Table")
            On Error GoTo 0
            
            If VT Is Nothing Then
                MsgBox "The 'Validation Table' worksheet was not found in the workbook.", vbExclamation
                Exit Sub
            End If
            
            Dim rSide As Integer 'row on daily for siding
            Dim rRoof As Integer 'row on daily for roof
            Dim rOthr As Integer 'row on daily for other
            Dim i As Integer 'row on timesheet
            Dim d As Integer 'row in daily for clearing
            Dim Day As Integer

            Dim TotalSide As Double 'total on daily for siding
            Dim TotalRoof As Double 'total on daily for roof
            Dim TotalOthr As Double 'total on daily for other
            Dim mergeRange As String ' Fixed typo from mergeRande to mergeRange

            ' Find last row in timesheet
            Dim lastRow As Integer
            On Error Resume Next
            lastRow = TimeCard.Range("Q65536").End(xlUp).Row
            On Error GoTo 0
            
            ' Validate lastRow is reasonable
            If lastRow < 6 Then
                MsgBox "Not enough data found in the TimeSheet. Please check your data.", vbExclamation
                Exit Sub
            End If

            ' Variables for cell values with proper error checking
            Dim cellValue As Variant
            Dim sidingVal As Double
            Dim gutterVal As Double
            Dim roofVal As Double
            Dim roofBuildVal As Double
            Dim driveVal As Double
            Dim officeVal As Double
            Dim otSidingVal As Double
            Dim otGutterVal As Double
            Dim otRoofVal As Double
            Dim otRoofBuildVal As Double
            Dim otDriveVal As Double
            Dim otOfficeVal As Double
            Dim dayName As String

            For Day = 3 To 14
                'select day of week
                On Error Resume Next
                cellValue = TimeCard.Cells(4, Day).Value
                On Error GoTo 0
                
                If IsEmpty(cellValue) And (Day > 3) Then
                     Day = Day + 1
                End If
                
                ' Get the day name for comparison
                On Error Resume Next
                dayName = CStr(TimeCard.Cells(4, Day).Value)
                On Error GoTo 0
                
                ' Compare sheet name with day name
                If Daily.Name = dayName Then

                    ' Remove old data section
                    On Error Resume Next
                    Dim maxRows As Integer
                    maxRows = Application.WorksheetFunction.RoundUp((lastRow / 3), 0)
                    ' Ensure maxRows is reasonable
                    If maxRows < 10 Or maxRows > 100 Then
                        maxRows = 50 ' Default to a reasonable value if calculation is off
                    End If
                    
                    For d = 10 To maxRows
                        ' Check for "Extra Row" cell before deletion
                        If Not IsEmpty(Daily.Cells(d, 13).Value) Then
                            If Daily.Cells(d, 13).Value = "Extra Row" Then
                                Daily.Rows(d).Delete
                                d = d - 1
                            ' Check for hours in range before clearing
                            ElseIf Not IsError(Daily.Cells(d, 7).Value) Then
                                If IsNumeric(Daily.Cells(d, 7).Value) Then
                                    If CDbl(Daily.Cells(d, 7).Value) > 0 And CDbl(Daily.Cells(d, 7).Value) < 25 Then
                                        Daily.Cells(d, 1).Value = Null
                                        Daily.Cells(d, 5).Value = Null
                                        Daily.Cells(d, 7).Value = Null
                                    End If
                                End If
                            End If
                        End If
                    Next d
                    On Error GoTo 0
                        
                    'This section is used to track the row for each of the three categories
                    rSide = 10
                    rRoof = 15
                    rOthr = 20
                    
                    For i = 6 To lastRow Step 13 'there are 13 rows per person
                        ' Validate employee name exists
                        On Error Resume Next
                        Dim employeeName As String
                        employeeName = Trim(CStr(TimeCard.Cells(i, 1).Value))
                        On Error GoTo 0
                        
                        If Len(employeeName) = 0 Then
                            ' Skip this employee record if name is missing
                            GoTo NextEmployee
                        End If
                        
                        ' Initialize all values to 0
                        sidingVal = 0
                        gutterVal = 0
                        roofVal = 0
                        roofBuildVal = 0
                        driveVal = 0
                        officeVal = 0
                        otSidingVal = 0
                        otGutterVal = 0
                        otRoofVal = 0
                        otRoofBuildVal = 0
                        otDriveVal = 0
                        otOfficeVal = 0
                        
                        ' Check if weekend by first letter of day name
                        Dim isWeekend As Boolean
                        isWeekend = (Left(CStr(TimeCard.Cells(4, Day).Value), 1) = "S")
                    
                        ' Get all relevant cell values with validation
                        On Error Resume Next
                        ' Regular time values
                        cellValue = TimeCard.Cells(i + 1, Day).Value
                        If Not IsError(cellValue) And IsNumeric(cellValue) Then
                            sidingVal = CDbl(cellValue)
                        End If
                        
                        cellValue = TimeCard.Cells(i + 4, Day).Value
                        If Not IsError(cellValue) And IsNumeric(cellValue) Then
                            gutterVal = CDbl(cellValue)
                        End If
                        
                        cellValue = TimeCard.Cells(i + 2, Day).Value
                        If Not IsError(cellValue) And IsNumeric(cellValue) Then
                            roofVal = CDbl(cellValue)
                        End If
                        
                        cellValue = TimeCard.Cells(i + 3, Day).Value
                        If Not IsError(cellValue) And IsNumeric(cellValue) Then
                            roofBuildVal = CDbl(cellValue)
                        End If
                        
                        cellValue = TimeCard.Cells(i + 5, Day).Value
                        If Not IsError(cellValue) And IsNumeric(cellValue) Then
                            driveVal = CDbl(cellValue)
                        End If
                        
                        cellValue = TimeCard.Cells(i + 6, Day).Value
                        If Not IsError(cellValue) And IsNumeric(cellValue) Then
                            officeVal = CDbl(cellValue)
                        End If
                        
                        ' For weekdays, also get OT values
                        If Not isWeekend And Day + 1 <= 14 Then
                            cellValue = TimeCard.Cells(i + 1, Day + 1).Value
                            If Not IsError(cellValue) And IsNumeric(cellValue) Then
                                otSidingVal = CDbl(cellValue)
                            End If
                            
                            cellValue = TimeCard.Cells(i + 4, Day + 1).Value
                            If Not IsError(cellValue) And IsNumeric(cellValue) Then
                                otGutterVal = CDbl(cellValue)
                            End If
                            
                            cellValue = TimeCard.Cells(i + 2, Day + 1).Value
                            If Not IsError(cellValue) And IsNumeric(cellValue) Then
                                otRoofVal = CDbl(cellValue)
                            End If
                            
                            cellValue = TimeCard.Cells(i + 3, Day + 1).Value
                            If Not IsError(cellValue) And IsNumeric(cellValue) Then
                                otRoofBuildVal = CDbl(cellValue)
                            End If
                            
                            cellValue = TimeCard.Cells(i + 5, Day + 1).Value
                            If Not IsError(cellValue) And IsNumeric(cellValue) Then
                                otDriveVal = CDbl(cellValue)
                            End If
                            
                            cellValue = TimeCard.Cells(i + 6, Day + 1).Value
                            If Not IsError(cellValue) And IsNumeric(cellValue) Then
                                otOfficeVal = CDbl(cellValue)
                            End If
                        End If
                        On Error GoTo 0
                        
                        ' Calculate totals based on time type and day type
                        If isWeekend Then
                            TotalSide = sidingVal + gutterVal
                            TotalRoof = roofVal + roofBuildVal
                            TotalOthr = driveVal + officeVal
                        Else
                            TotalSide = sidingVal + gutterVal + otSidingVal + otGutterVal
                            TotalRoof = roofVal + roofBuildVal + otRoofVal + otRoofBuildVal
                            TotalOthr = driveVal + officeVal + otDriveVal + otOfficeVal
                        End If
                        
                        ' Ensure totals are not negative
                        If TotalSide < 0 Then TotalSide = 0
                        If TotalRoof < 0 Then TotalRoof = 0
                        If TotalOthr < 0 Then TotalOthr = 0
                            
                        ' Process siding and gutter time if above threshold
                        If TotalSide > 0.1 Then
                            ' Check if we need to add a new row
                            If rSide > 13 Then
                                On Error Resume Next
                                Daily.Rows(rSide).Insert
                                
                                mergeRange = "A" & rSide & ":D" & rSide
                                Daily.Range(mergeRange).Merge

                                mergeRange = "E" & rSide & ":F" & rSide
                                Daily.Range(mergeRange).Merge
                                
                                mergeRange = "I" & rSide & ":J" & rSide
                                Daily.Range(mergeRange).Merge
                                
                                ' Update row tracking when inserting a new row
                                rRoof = rRoof + 1
                                rOthr = rOthr + 1
                                
                                Daily.Cells(rSide, 13).Value = "Extra Row"
                                On Error GoTo 0
                            End If

                            On Error Resume Next
                            Daily.Cells(rSide, 1).Value = employeeName 'name
                            
                            ' Check for foreman status
                            Dim isForeman As Boolean
                            isForeman = False
                            
                            cellValue = TimeCard.Cells(i + 7, Day).Value
                            If Not IsError(cellValue) Then
                                If cellValue = True Then
                                    isForeman = True
                                End If
                            End If
                            
                            If isForeman Then
                                Daily.Cells(rSide, 5).Value = "Foreman"
                            Else
                                ' Lookup position with error handling
                                Dim lookupResult As Variant
                                lookupResult = Application.VLookup(employeeName, VT.Range("a1:b331"), 2, False)
                                
                                If Not IsError(lookupResult) Then
                                    Dim splitResult As Variant
                                    splitResult = Split(CStr(lookupResult), " ")
                                    If UBound(splitResult) >= 0 Then
                                        Daily.Cells(rSide, 5).Value = splitResult(0)
                                    Else
                                        Daily.Cells(rSide, 5).Value = "Employee"
                                    End If
                                Else
                                    Daily.Cells(rSide, 5).Value = "Employee"
                                End If
                            End If
                            
                            ' Validate hours before adding
                            If TotalSide > 0 And TotalSide < 24 Then
                                Daily.Cells(rSide, 7).Value = TotalSide
                            Else
                                Daily.Cells(rSide, 7).Value = WorksheetFunction.Min(TotalSide, 24)
                            End If
                            
                            rSide = rSide + 1
                            On Error GoTo 0
                        End If

                        ' Process roofing time if above threshold
                        If TotalRoof > 0.1 Then
                            ' Modified condition for adding rows to use fixed threshold
                            ' Original logic: rRoof > rSide + 4 And (rSide > 13)
                            If rRoof > 18 Then
                                On Error Resume Next
                                Daily.Rows(rRoof).Insert
                                mergeRange = "A" & rRoof & ":D" & rRoof
                                Daily.Range(mergeRange).Merge

                                mergeRange = "E" & rRoof & ":F" & rRoof
                                Daily.Range(mergeRange).Merge
                                
                                mergeRange = "I" & rRoof & ":J" & rRoof
                                Daily.Range(mergeRange).Merge
                                
                                rOthr = rOthr + 1
                                Daily.Cells(rRoof, 13).Value = "Extra Row"  'marked so that this row can be deleted next time
                                On Error GoTo 0
                            End If

                            On Error Resume Next
                            Daily.Cells(rRoof, 1).Value = employeeName 'name
                            
                            ' Check foreman status - consistent for all categories
                            isForeman = False
                            cellValue = TimeCard.Cells(i + 7, Day).Value
                            If Not IsError(cellValue) Then
                                If cellValue = True Then
                                    isForeman = True
                                End If
                            End If
                            
                            If isForeman Then
                                Daily.Cells(rRoof, 5).Value = "Foreman"
                            Else
                                ' Lookup with error handling
                                Dim lookupResult2 As Variant
                                lookupResult2 = Application.VLookup(employeeName, VT.Range("a1:b331"), 2, False)
                                
                                If Not IsError(lookupResult2) Then
                                    Dim splitResult2 As Variant
                                    splitResult2 = Split(CStr(lookupResult2), " ")
                                    If UBound(splitResult2) >= 0 Then
                                        Daily.Cells(rRoof, 5).Value = splitResult2(0)
                                    Else
                                        Daily.Cells(rRoof, 5).Value = "Employee"
                                    End If
                                Else
                                    Daily.Cells(rRoof, 5).Value = "Employee"
                                End If
                            End If
                            
                            ' Validate hours are reasonable
                            If TotalRoof > 0 And TotalRoof < 24 Then
                                Daily.Cells(rRoof, 7).Value = TotalRoof
                            Else
                                Daily.Cells(rRoof, 7).Value = WorksheetFunction.Min(TotalRoof, 24)
                            End If
                            
                            rRoof = rRoof + 1
                            On Error GoTo 0
                        End If
                        
                        ' Process other time if above threshold
                        If TotalOthr > 0.1 Then
                            ' Modified condition for adding rows to use fixed threshold
                            ' Original logic: rOthr > rRoof + 4 And (rRoof > 18)
                            If rOthr > 23 Then
                                On Error Resume Next
                                Daily.Rows(rOthr).Insert
                                mergeRange = "A" & rOthr & ":D" & rOthr
                                Daily.Range(mergeRange).Merge

                                mergeRange = "E" & rOthr & ":F" & rOthr
                                Daily.Range(mergeRange).Merge
                                
                                mergeRange = "I" & rOthr & ":J" & rOthr
                                Daily.Range(mergeRange).Merge
                                
                                Daily.Cells(rOthr, 13).Value = "Extra Row"  'marked so that this row can be deleted next time
                                On Error GoTo 0
                            End If
                        
                            On Error Resume Next
                            Daily.Cells(rOthr, 1).Value = employeeName 'name
                            
                            ' Check foreman status - consistent with other categories
                            ' Fixed issue: original used i+8 here instead of i+7
                            isForeman = False
                            cellValue = TimeCard.Cells(i + 7, Day).Value
                            If Not IsError(cellValue) Then
                                If cellValue = True Then
                                    isForeman = True
                                End If
                            End If
                            
                            If isForeman Then
                                Daily.Cells(rOthr, 5).Value = "Foreman"
                            Else
                                ' Lookup with error handling
                                Dim lookupResult3 As Variant
                                lookupResult3 = Application.VLookup(employeeName, VT.Range("a1:b331"), 2, False)
                                
                                If Not IsError(lookupResult3) Then
                                    Dim splitResult3 As Variant
                                    splitResult3 = Split(CStr(lookupResult3), " ")
                                    If UBound(splitResult3) >= 0 Then
                                        Daily.Cells(rOthr, 5).Value = splitResult3(0)
                                    Else
                                        Daily.Cells(rOthr, 5).Value = "Employee"
                                    End If
                                Else
                                    Daily.Cells(rOthr, 5).Value = "Employee"
                                End If
                            End If

                            ' Validate hours are reasonable
                            If TotalOthr > 0 And TotalOthr < 24 Then
                                Daily.Cells(rOthr, 7).Value = TotalOthr
                            Else
                                Daily.Cells(rOthr, 7).Value = WorksheetFunction.Min(TotalOthr, 24)
                            End If

                            rOthr = rOthr + 1
                            On Error GoTo 0
                        End If

NextEmployee:
                    Next i
                    
                    ' Only process one daily (the matching one), then exit the loop
                    Exit For
                End If
            Next Day

            ' Save workbook with error handling
            On Error Resume Next
            TimeCardBook.Save
            If Err.Number <> 0 Then
                MsgBox "Warning: Could not save the workbook. Please save manually.", vbExclamation
                hasErrors = True
            End If
            On Error GoTo 0
            
            If hasErrors Then
                MsgBox "Time added to Daily Report with some errors. Please check before creating PDFs", vbExclamation
            Else
                MsgBox "Time added to Daily Report", vbInformation
            End If
         Else
            MsgBox "Week must begin on a Monday. Please try again.", vbExclamation
        End If
    Else    ' User chose No.
        MsgBox "Report Not Filled", vbInformation
    End If
End Sub
