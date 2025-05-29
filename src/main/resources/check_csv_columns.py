#!/usr/bin/env python3

import csv
import sys
from pathlib import Path

def check_csv_columns(csv_file):
    """
    Check CSV file for correct number of columns and formatting issues.
    
    Args:
        csv_file (str): Path to the CSV file
    """
    EXPECTED_COLUMNS = 21
    EXPECTED_HEADER = [
        'clientId', 'type', 'amount', 'transactionDate', 'status',
        'fundName', 'fromFund', 'toFund', 'frequency', 'startDate',
        'endDate', 'nextTransactionDate', 'installmentNumber', 'totalInstallments',
        'isRecurring', 'schemeCode', 'assetClass', 'units', 'navAtTransactionTime',
        'mode', 'remarks'
    ]
    
    try:
        with open(csv_file, 'r', encoding='utf-8') as f:
            # Read the file content
            content = f.read()
            
            # Check for BOM
            if content.startswith('\ufeff'):
                print("Warning: File contains BOM marker")
            
            # Check for trailing commas at end of lines
            lines = content.split('\n')
            for i, line in enumerate(lines, 1):
                if line.strip().endswith(','):
                    print(f"Warning: Row {i} has trailing comma")
            
            # Reset file pointer
            f.seek(0)
            
            # Use CSV reader for proper handling of quoted fields
            reader = csv.reader(f)
            header = next(reader)  # Get header row
            
            # Check header
            if len(header) != EXPECTED_COLUMNS:
                print(f"Error: Header has {len(header)} columns, expected {EXPECTED_COLUMNS}")
                print("Header:", header)
            
            # Check if header matches expected column names
            if header != EXPECTED_HEADER:
                print("Warning: Header column names don't match expected names")
                print("Expected:", EXPECTED_HEADER)
                print("Found:", header)
            
            # Check each row
            for i, row in enumerate(reader, 2):  # Start from 2 as header is row 1
                if not row:  # Skip empty rows
                    continue
                    
                if len(row) != EXPECTED_COLUMNS:
                    print(f"Error: Row {i} has {len(row)} columns, expected {EXPECTED_COLUMNS}")
                    print(f"Row content: {row}")
                    
                # Check for empty required fields
                if not row[0]:  # clientId
                    print(f"Warning: Row {i} missing clientId")
                if not row[1]:  # type
                    print(f"Warning: Row {i} missing type")
                if not row[2]:  # amount
                    print(f"Warning: Row {i} missing amount")
                    
                # Check date format
                date_fields = [row[3], row[9], row[10], row[11]]  # transactionDate, startDate, endDate, nextTransactionDate
                for date in date_fields:
                    if date and not date.strip().startswith(('2023-', '2024-', '2025-')):
                        print(f"Warning: Row {i} has potentially invalid date format: {date}")

    except FileNotFoundError:
        print(f"Error: File {csv_file} not found")
    except Exception as e:
        print(f"Error processing file: {str(e)}")

if __name__ == "__main__":
    # Get CSV file path from command line or use default
    csv_file = sys.argv[1] if len(sys.argv) > 1 else "Sample_Transactions_Full.csv"
    
    # If relative path is provided, make it relative to script location
    if not Path(csv_file).is_absolute():
        csv_file = Path(__file__).parent / csv_file
    
    print(f"Checking CSV file: {csv_file}")
    check_csv_columns(csv_file)
    print("CSV check completed") 