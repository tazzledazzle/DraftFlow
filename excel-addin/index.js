// Excel Add-In Authentication Module
const authModule = (function() {
    let currentToken = null;

    /**
     * Authenticate with the application server using a project token
     * @param {string} token - The project-specific JWT token
     * @returns {Promise<boolean>} - Whether authentication was successful
     */
    async function authenticate(token) {
        try {
            // Validate the token with the server
            const response = await fetch(`${API_BASE_URL}/api/excel-auth/validate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ token })
            });

            if (!response.ok) {
                throw new Error('Invalid token');
            }

            // Store the token
            currentToken = token;
            await storeTokenInSettings(token);

            return true;
        } catch (error) {
            console.error('Authentication error:', error);
            return false;
        }
    }

    /**
     * Store the token in Excel document settings
     * @param {string} token - The JWT token to store
     */
    async function storeTokenInSettings(token) {
        await Office.context.document.settings.set('authToken', token);
        await Office.context.document.settings.saveAsync();
    }

    /**
     * Load a previously stored token from Excel document settings
     * @returns {Promise<string|null>} - The stored token or null if not found
     */
    async function loadStoredToken() {
        return new Promise((resolve) => {
            const token = Office.context.document.settings.get('authToken');
            currentToken = token || null;
            resolve(currentToken);
        });
    }

    /**
     * Get the current token for API requests
     * @returns {string|null} - The current JWT token
     */
    function getToken() {
        return currentToken;
    }

    /**
     * Clear the stored authentication token
     */
    async function logout() {
        currentToken = null;
        await Office.context.document.settings.remove('authToken');
        await Office.context.document.settings.saveAsync();
    }

    // Public API
    return {
        authenticate,
        getToken,
        loadStoredToken,
        logout
    };
})();

// Export the module
export default authModule;

/**
 * Collects timesheet data from the Excel sheet and submits to the server
 */
async function submitTimesheetData() {
    try {
        const token = authModule.getToken();
        if (!token) {
            showErrorNotification("Please authenticate first");
            return;
        }

        await Excel.run(async (context) => {
            // Get the active worksheet
            const sheet = context.workbook.worksheets.getActiveWorksheet();

            // Load employee name
            const employeeNameRange = sheet.getRange("B3");
            employeeNameRange.load("values");

            // Load date range
            const dateRanges = sheet.getRange("A5:A9");
            dateRanges.load("values");

            // Load task selections
            const taskRanges = sheet.getRange("C5:C9");
            taskRanges.load("values");

            // Load hours worked
            const hoursRanges = sheet.getRange("D5:D9");
            hoursRanges.load("values");

            // Load notes
            const notesRanges = sheet.getRange("E5:E9");
            notesRanges.load("values");

            await context.sync();

            // Get employee name
            const employeeName = employeeNameRange.values[0][0];
            if (!employeeName) {
                throw new Error("Employee name is required");
            }

            // Prepare timesheet entries
            const entries = [];

            for (let i = 0; i < 5; i++) {
                // Extract day of week and convert to date
                const dayOfWeek = dateRanges.values[i][0];
                const hours = hoursRanges.values[i][0];
                const taskCell = taskRanges.values[i][0];
                const notes = notesRanges.values[i][0];

                // Skip empty rows
                if (!taskCell || !hours) {
                    continue;
                }

                // Extract task ID from the format "123 - Task Name"
                const taskId = parseInt(taskCell.split(' - ')[0], 10);
                if (isNaN(taskId)) {
                    throw new Error(`Invalid task format in row ${i + 5}`);
                }

                // Calculate the actual date based on day of week
                const workDate = calculateDateFromDayOfWeek(dayOfWeek);

                entries.push({
                    taskId,
                    employeeName,
                    hoursWorked: parseFloat(hours),
                    workDate: workDate.toISOString().split('T')[0], // YYYY-MM-DD format
                    notes: notes || null
                });
            }

            // Submit entries to the server
            if (entries.length > 0) {
                const response = await fetch(`${API_BASE_URL}/api/timesheets/batch`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(entries)
                });

                if (!response.ok) {
                    throw new Error(`Server error: ${response.status}`);
                }

                // Update status cells
                const statusRanges = sheet.getRange("F5:F9");
                const statusValues = [];

                for (let i = 0; i < 5; i++) {
                    if (i < entries.length) {
                        statusValues.push(["Submitted"]);
                    } else {
                        statusValues.push(["Not Submitted"]);
                    }
                }

                statusRanges.values = statusValues;
                statusRanges.format.font.color = "#007700";

                await context.sync();
                showSuccessNotification(`${entries.length} timesheet entries submitted`);
            } else {
                showWarningNotification("No timesheet entries to submit");
            }
        });
    } catch (error) {
        console.error("Error submitting timesheet:", error);
        showErrorNotification(`Submission failed: ${error.message}`);
    }
}

/**
 * Helper function to calculate date from day of week
 * @param {string} dayOfWeek - The day of week (e.g., "Mon", "Tue")
 * @returns {Date} - The date object for that day in the current week
 */
function calculateDateFromDayOfWeek(dayOfWeek) {
    const days = { "Mon": 1, "Tue": 2, "Wed": 3, "Thu": 4, "Fri": 5, "Sat": 6, "Sun": 0 };
    const dayNumber = days[dayOfWeek];

    if (dayNumber === undefined) {
        throw new Error(`Invalid day of week: ${dayOfWeek}`);
    }

    const today = new Date();
    const currentDayNumber = today.getDay(); // 0 = Sunday, 1 = Monday, etc.
    const diff = dayNumber - currentDayNumber;

    // Calculate the date for the specified day in the current week
    const targetDate = new Date(today);
    targetDate.setDate(today.getDate() + diff + (diff < 0 ? 7 : 0));

    return targetDate;
}

