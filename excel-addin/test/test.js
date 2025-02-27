// Mock the Office.js API
global.Office = {
    context: {
        document: {
            settings: {
                get: jest.fn(),
                set: jest.fn(),
                saveAsync: jest.fn((callback) => callback()),
                remove: jest.fn()
            }
        }
    }
};

global.Excel = {
    run: jest.fn((callback) => {
        const context = {
            sync: jest.fn().mockResolvedValue(undefined),
            workbook: {
                worksheets: {
                    getActiveWorksheet: jest.fn().mockReturnValue({
                        getRange: jest.fn().mockReturnValue({
                            load: jest.fn(),
                            values: [["Test Value"]],
                            dataValidation: {
                                rule: {},
                                clear: jest.fn()
                            },
                            format: {
                                fill: { color: "" },
                                font: { color: "" }
                            }
                        })
                    })
                }
            }
        };
        return callback(context);
    })
};

// Import the module to test
import authModule from '../src/auth/auth';

// Mock fetch API
global.fetch = jest.fn(() =>
    Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ success: true })
    })
);

describe('Authentication Module', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('authenticate should store token when successful', async () => {
        // Arrange
        const testToken = 'test-token-123';

        // Act
        const result = await authModule.authenticate(testToken);

        // Assert
        expect(result).toBe(true);
        expect(global.Office.context.document.settings.set).toHaveBeenCalledWith('authToken', testToken);
        expect(global.Office.context.document.settings.saveAsync).toHaveBeenCalled();
        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/excel-auth/validate'),
            expect.objectContaining({
                method: 'POST',
                body: JSON.stringify({ token: testToken })
            })
        );
    });

    test('getToken should return the current token', async () => {
        // Arrange
        const testToken = 'test-token-123';
        await authModule.authenticate(testToken);

        // Act
        const token = authModule.getToken();

        // Assert
        expect(token).toBe(testToken);
    });

    test('logout should clear the token', async () => {
        // Arrange
        const testToken = 'test-token-123';
        await authModule.authenticate(testToken);

        // Act
        await authModule.logout();

        // Assert
        expect(authModule.getToken()).toBeNull();
        expect(global.Office.context.document.settings.remove).toHaveBeenCalledWith('authToken');
        expect(global.Office.context.document.settings.saveAsync).toHaveBeenCalled();
    });

    test('loadStoredToken should load token from settings', async () => {
        // Arrange
        const testToken = 'stored-token-456';
        global.Office.context.document.settings.get.mockReturnValue(testToken);

        // Act
        const token = await authModule.loadStoredToken();

        // Assert
        expect(token).toBe(testToken);
        expect(global.Office.context.document.settings.get).toHaveBeenCalledWith('authToken');
        expect(authModule.getToken()).toBe(testToken);
    });
});

