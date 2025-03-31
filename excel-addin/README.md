# Excel Add-In

## Excel Integration Layer
### Office JS Add-In Structure

The Excel Add-In follows the Microsoft Office Add-In architecture:


```shell
excel-addin/
├── assets/
│   ├── logo-16.png
│   ├── logo-32.png
│   └── logo-80.png
├── src/
│   ├── taskpane/
│   │   ├── taskpane.html
│   │   ├── taskpane.css
│   │   └── taskpane.js
│   ├── auth/
│   │   ├── auth.js
│   │   └── token-storage.js
│   ├── api/
│   │   ├── api-client.js
│   │   ├── project-api.js
│   │   ├── task-api.js
│   │   └── timesheet-api.js
│   └── helpers/
│       ├── excel-utilities.js
│       └── validation.js
├── manifest.xml
└── package.json

````

The add-in provides these main capabilities:
* Authentication: Handles login and token management
* Project Selection: Allows users to select an active project
* Task Dropdowns: Populates Excel cells with task options
* Data Submission: Sends completed timesheet data to the server


Key code for populating the task dropdowns:


```javascript
/**
* Populates task dropdown lists in designated cells
* @param {string} projectId - The ID of the selected project
  */
  async function populateTaskDropdowns(projectId) {
  try {
  // First, fetch tasks for the selected project
  const tasks = await taskApi.getTasksForProject(projectId);

       // Format tasks for dropdown (ID + Name format)
       const taskOptions = tasks.map(task => `${task.id} - ${task.name}`);

       // Now apply to Excel
       await Excel.run(async (context) => {
           // Get the active worksheet
           const sheet = context.workbook.worksheets.getActiveWorksheet();

           // Define the cells that should have task dropdowns (C5:C19)
           const taskCells = sheet.getRange("C5:C19");

           // Apply validation with dropdown list
           taskCells.dataValidation.clear();
           taskCells.dataValidation.rule = {
               list: {
                   inCellDropDown: true,
                   source: taskOptions.join(",")
               }
           };

           // Apply special formatting to these cells
           taskCells.format.fill.color = "#e6f2ff";
           taskCells.format.font.color = "#000000";

           await context.sync();
           console.log("Task dropdowns created successfully");
       });
  } catch (error) {
  console.error("Error creating task dropdowns:", error);
  showErrorNotification("Failed to load tasks. Please try again.");
  }
  }
```
The Excel Add-In uses the Office JavaScript API to interact with Excel and provide a seamless experience for users. The add-in is structured with separate modules for authentication, API calls, and helper functions to keep the code organized and maintainable. The manifest.xml file defines the add-in's settings and capabilities, while the package.json file manages dependencies and build scripts.