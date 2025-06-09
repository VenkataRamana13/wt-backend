#!/usr/bin/env python3

from collections import defaultdict
import csv
from datetime import datetime
import os

def check_duplicates():
    # Use absolute path to amfi.txt
    amfi_file = '/home/ramana/wealthTree/wt-platform-backend-2/src/main/resources/amfi.txt'
    
    # Dictionary to store entries by scheme_code and date
    entries = defaultdict(list)
    duplicates = defaultdict(int)
    
    print(f"Reading file from: {amfi_file}")
    
    # Read the AMFI NAV file
    with open(amfi_file, 'r') as file:
        # Skip header if it exists
        header = file.readline()
        print(f"Header: {header.strip()}")
        
        # Process each line
        for line_num, line in enumerate(file, 2):  # Start from line 2 as we skipped header
            try:
                # Split by semicolon and clean whitespace
                fields = [field.strip() for field in line.split(';')]
                
                if len(fields) >= 6:  # Ensure we have enough fields
                    scheme_code = fields[0]
                    nav_date = fields[5]
                    scheme_name = fields[3]
                    nav_value = fields[4]
                    
                    # Create a key combining scheme_code and date
                    key = (scheme_code, nav_date)
                    
                    # Store the entry with line number and full details
                    entries[key].append({
                        'line_num': line_num,
                        'scheme_name': scheme_name,
                        'nav_value': nav_value
                    })
                    
                    # If we found more than one entry for this key, increment duplicate count
                    if len(entries[key]) > 1:
                        duplicates[key] = len(entries[key])
            
            except Exception as e:
                print(f"Error processing line {line_num}: {str(e)}")
                continue

    # Print results
    print("\n=== Duplicate Analysis Results ===")
    
    if duplicates:
        print("\nFound duplicate entries:")
        print("Scheme Code | Date | Count | Details")
        print("-" * 80)
        
        for (scheme_code, date), count in sorted(duplicates.items()):
            print(f"\nScheme Code: {scheme_code}")
            print(f"Date: {date}")
            print(f"Number of entries: {count}")
            print("Entries found on lines:")
            
            for entry in entries[(scheme_code, date)]:
                print(f"  Line {entry['line_num']}: {entry['scheme_name']} - NAV: {entry['nav_value']}")
            
            print("-" * 40)
    else:
        print("\nNo duplicates found!")

    # Print summary
    total_unique_schemes = len(set(code for code, _ in entries.keys()))
    total_entries = sum(len(entry_list) for entry_list in entries.values())
    total_duplicates = sum(count - 1 for count in duplicates.values())
    
    print("\n=== Summary ===")
    print(f"Total unique schemes: {total_unique_schemes}")
    print(f"Total entries: {total_entries}")
    print(f"Total duplicate entries: {total_duplicates}")

if __name__ == "__main__":
    check_duplicates() 