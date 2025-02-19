import requests
import json
import time
import os
from base64 import b64encode
from datetime import datetime, timedelta


class AutodeskDesignAutomation:
    def __init__(self, client_id, client_secret):
        """Initialize the Design Automation client with credentials."""
        self.client_id = client_id
        self.client_secret = client_secret
        self.access_token = None
        self.token_expiry = None

        # API endpoints
        self.auth_url = "https://developer.api.autodesk.com/authentication/v2/token"
        self.da_url = "https://developer.api.autodesk.com/da/us-east/v3"

    def get_access_token(self):
        """Get or refresh the access token for API calls."""
        # Check if we need a new token
        if (not self.access_token or
                not self.token_expiry or
                datetime.now() >= self.token_expiry):
            # Prepare authentication request
            auth_data = {
                'client_id': self.client_id,
                'client_secret': self.client_secret,
                'grant_type': 'client_credentials',
                'scope': 'code:all data:read data:write bucket:read bucket:create'
            }

            # Request new token
            response = requests.post(self.auth_url, data=auth_data)
            response.raise_for_status()

            token_data = response.json()
            self.access_token = token_data['access_token']
            # Set token expiry (subtract 5 minutes for safety margin)
            self.token_expiry = datetime.now() + timedelta(
                seconds=token_data['expires_in'] - 300)

        return self.access_token

    def create_activity(self):
        """Create a new Design Automation activity for DWG conversion."""
        headers = {
            'Authorization': f'Bearer {self.get_access_token()}',
            'Content-Type': 'application/json'
        }

        # Define the activity
        activity_data = {
            'commandLine': [
                '$(engine.path)\\accoreconsole.exe /i $(args[inputFile].path) /al $(appbundles[MetadataExtractor].path) /s $(settings[script].path)'
            ],
            'parameters': {
                'inputFile': {
                    'verb': 'get',
                    'description': 'Input DWG file',
                    'required': True
                },
                'outputJson': {
                    'verb': 'put',
                    'description': 'Output JSON metadata',
                    'required': True
                }
            },
            'engine': 'Autodesk.AutoCAD+24',
            'appbundles': ['MetadataExtractor.bundle'],
            'settings': {
                'script': 'ExtractMetadata.scr'
            }
        }

        response = requests.post(
            f'{self.da_url}/activities/DwgToJson',
            headers=headers,
            json=activity_data
        )
        response.raise_for_status()
        return response.json()

    def upload_file(self, file_path):
        """Upload a DWG file to Autodesk storage."""
        headers = {
            'Authorization': f'Bearer {self.get_access_token()}'
        }

        # Get signed URL for upload
        signed_url_response = requests.get(
            f'{self.da_url}/signedurls/dwg',
            headers=headers,
            params={'format': 'binary'}
        )
        signed_url_response.raise_for_status()
        signed_url_data = signed_url_response.json()

        # Upload file to signed URL
        with open(file_path, 'rb') as f:
            upload_response = requests.put(
                signed_url_data['signedUrl'],
                data=f,
                headers={'Content-Type': 'application/octet-stream'}
            )
            upload_response.raise_for_status()

        return signed_url_data['url']

    def submit_workitem(self, input_url):
        """Submit a work item to process the DWG file."""
        headers = {
            'Authorization': f'Bearer {self.get_access_token()}',
            'Content-Type': 'application/json'
        }

        # Prepare work item data
        workitem_data = {
            'activityId': 'DwgToJson',
            'arguments': {
                'inputFile': {
                    'url': input_url
                },
                'outputJson': {
                    'verb': 'put',
                    'url': ''  # Will be filled by the service
                }
            }
        }

        response = requests.post(
            f'{self.da_url}/workitems',
            headers=headers,
            json=workitem_data
        )
        response.raise_for_status()
        return response.json()

    def check_workitem_status(self, workitem_id):
        """Check the status of a work item."""
        headers = {
            'Authorization': f'Bearer {self.get_access_token()}'
        }

        response = requests.get(
            f'{self.da_url}/workitems/{workitem_id}',
            headers=headers
        )
        response.raise_for_status()
        return response.json()

    def download_result(self, result_url, output_path):
        """Download the resulting JSON file."""
        headers = {
            'Authorization': f'Bearer {self.get_access_token()}'
        }

        response = requests.get(result_url, headers=headers)
        response.raise_for_status()

        with open(output_path, 'wb') as f:
            f.write(response.content)

    def process_dwg(self, input_file, output_file):
        """Process a DWG file and convert it to JSON metadata."""
        try:
            print("1. Uploading DWG file...")
            input_url = self.upload_file(input_file)

            print("2. Creating work item...")
            workitem = self.submit_workitem(input_url)
            workitem_id = workitem['id']

            print("3. Monitoring progress...")
            while True:
                status = self.check_workitem_status(workitem_id)
                if status['status'] == 'success':
                    result_url = status['reportUrl']
                    break
                elif status['status'] in ['failed', 'cancelled']:
                    raise Exception(f"Work item failed: {status.get('error', 'Unknown error')}")

                time.sleep(5)  # Wait before checking again

            print("4. Downloading result...")
            self.download_result(result_url, output_file)
            print(f"Conversion complete! JSON saved to: {output_file}")

        except Exception as e:
            print(f"Error processing DWG file: {str(e)}")
            raise


def main():
    # Load credentials from environment variables
    client_id = "38rEZvDrOmj2snOtNqy5Rwn34AzzH0sFGTs7XA8If9xXGy1K"
    client_secret = "UZrvsiBtL4XMSkeHo33fWABrrSxCoGvqqlyfB95dWNZ1yu7W9DnYAkE1LONonNnY"

    if not client_id or not client_secret:
        raise ValueError("Please set AUTODESK_CLIENT_ID and AUTODESK_CLIENT_SECRET environment variables")

    # Initialize the client
    da_client = AutodeskDesignAutomation(client_id, client_secret)

    # Process the DWG file
    input_file = "resources/dwgFiles/2023-Childrens Hospital Mtn Lvl 7 (6495)-XREF-Base Details.dwg"
    output_file = "metadata.json"

    da_client.process_dwg(input_file, output_file)


if __name__ == "__main__":
    main()