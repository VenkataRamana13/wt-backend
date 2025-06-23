#!/usr/bin/env python3

from datetime import datetime, timedelta
from collections import defaultdict
import os
import sys

def analyze_amfi_entries(target_date_str=None):
    """
    Analyze AMFI entries for a specific date.
    If no date provided, defaults to today's date.
    
    Args:
        target_date_str (str): Date in format 'dd-MMM-yyyy' (e.g., '08-Jun-2025')
                              If None, uses today's date
    """
    # Get target date if not provided
    if not target_date_str:
        target_date = datetime.now()
        target_date_str = target_date.strftime('%d-%b-%Y')
    
    # File path
    script_dir = os.path.dirname(os.path.abspath(__file__))
    amfi_file = os.path.join(script_dir, 'amfi.txt')
    
    print(f"\nAnalyzing AMFI entries for date: {target_date_str}")
    print("=" * 50)
    
    # Counters
    total_entries = 0
    date_entries = 0
    schemes_by_amc = defaultdict(int)
    
    try:
        with open(amfi_file, 'r') as file:
            current_amc = "Unknown AMC"
            
            for line in file:
                line = line.strip()
                
                # Skip empty lines
                if not line:
                    continue
                
                # Check if this is an AMC name line
                if "Mutual Fund" in line and ";" not in line:
                    current_amc = line.strip()
                    continue
                
                # Skip header or category lines
                if ";" not in line or line.startswith("Scheme Code"):
                    continue
                
                # Process NAV entry
                fields = line.split(';')
                if len(fields) >= 6:
                    total_entries += 1
                    nav_date = fields[5].strip()
                    
                    if nav_date == target_date_str:
                        date_entries += 1
                        schemes_by_amc[current_amc] += 1
        
        # Print results
        print(f"\nTotal entries in file: {total_entries}")
        print(f"Entries for {target_date_str}: {date_entries}")
        
        if date_entries > 0:
            print("\nBreakdown by AMC:")
            print("-" * 30)
            for amc, count in schemes_by_amc.items():
                if count > 0:
                    print(f"{amc}: {count} schemes")
        
    except FileNotFoundError:
        print(f"Error: AMFI file not found at {amfi_file}")
        return
    except Exception as e:
        print(f"Error processing file: {str(e)}")
        return

def main():
    # Get today and yesterday's dates
    today = datetime.now()
    yesterday = today - timedelta(days=1)
    
    # Format dates as required by AMFI (dd-MMM-yyyy)
    today_str = today.strftime('%d-%b-%Y')
    yesterday_str = yesterday.strftime('%d-%b-%Y')
    
    # Analyze both days
    print("\nAMFI Entry Analysis")
    print("=" * 50)
    
    analyze_amfi_entries(today_str)
    analyze_amfi_entries(yesterday_str)

if __name__ == "__main__":
    main() 