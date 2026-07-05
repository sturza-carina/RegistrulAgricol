import { Component, Input, OnInit, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface TableColumn {
  field: string;
  header: string;
  type?: 'text' | 'badge' | 'avatar' | 'date'; 
  format?: (val: any, row?: any) => string;
  badgeClasses?: Record<string, string>; 
  subField?: string; // e.g. for avatar handle or text subline
}

export interface TableFilter {
  field: string;
  label: string;
  type: 'search' | 'select';
  options?: { label: string, value: any }[];
  searchFields?: string[];
}

export interface TableAction {
  icon: 'view' | 'edit' | 'delete' | 'add' | 'history';
  tooltip: string;
  action: (row: any, event: Event) => void;
  showIf?: (row: any) => boolean;
}

import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-generic-table',
  standalone: true,
  imports: [CommonModule, FormsModule, AppTranslatePipe],
  templateUrl: './generic-table.component.html',
  styleUrls: ['./generic-table.component.css']
})
export class GenericTableComponent implements OnInit, OnChanges {
  @Input() data: any[] = [];
  @Input() columns: TableColumn[] = [];
  @Input() actions: TableAction[] = [];
  @Input() filters: TableFilter[] = [];
  @Input() title: string = '';
  @Input() itemsPerPage: number = 6;
  @Input() emptyMessage: string = 'Nicio înregistrare găsită.';
  @Input() primaryActionText?: string;
  
  @Input() serverSide: boolean = false;
  @Input() totalServerPages: number = 1;
  @Input() currentServerPage: number = 1;
  
  @Output() rowClick = new EventEmitter<any>();
  @Output() primaryAction = new EventEmitter<void>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() filterChange = new EventEmitter<Record<string, any>>();

  filteredData: any[] = [];
  currentPage = 1;
  filterValues: Record<string, any> = {};

  ngOnInit() {
    this.initFilters();
    if (!this.serverSide) {
      this.applyFilters();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data'] && this.data) {
      if (!this.serverSide) {
        this.applyFilters();
      } else {
        this.filteredData = this.data;
      }
    }
    if (changes['currentServerPage']) {
      this.currentPage = this.currentServerPage;
    }
  }

  initFilters() {
    if (this.filters) {
      this.filters.forEach(f => {
        this.filterValues[f.field] = '';
      });
    }
  }

  applyFilters() {
    if (this.serverSide) {
      this.filterChange.emit(this.filterValues);
      return;
    }

    let result = this.data || [];

    if (this.filters && this.filters.length > 0) {
      for (const filter of this.filters) {
        const val = this.filterValues[filter.field];
        if (val === undefined || val === null || val === '') continue;

        if (filter.type === 'search' && filter.searchFields) {
          const searchVal = String(val).toLowerCase();
          result = result.filter(item => 
            filter.searchFields!.some(f => {
              const fieldVal = this.getNestedValue(item, f);
              return fieldVal && String(fieldVal).toLowerCase().includes(searchVal);
            })
          );
        } else if (filter.type === 'select') {
           result = result.filter(item => {
             const fieldVal = this.getNestedValue(item, filter.field);
             return fieldVal === val;
           });
        }
      }
    }

    this.filteredData = result;
    // reset to page 1 when filtering
    this.currentPage = 1;
  }

  get paginatedItems() {
    if (this.serverSide) {
      return this.data;
    }
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredData.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages() {
    if (this.serverSide) {
      return this.totalServerPages;
    }
    return Math.ceil(this.filteredData.length / this.itemsPerPage) || 1;
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      if (this.serverSide) {
        this.pageChange.emit(this.currentPage + 1);
      } else {
        this.currentPage++;
      }
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      if (this.serverSide) {
        this.pageChange.emit(this.currentPage - 1);
      } else {
        this.currentPage--;
      }
    }
  }

  getNestedValue(obj: any, path: string): any {
    if (!path || !obj) return undefined;
    return path.split('.').reduce((acc, part) => acc && acc[part], obj);
  }

  getCellValue(row: any, col: TableColumn): any {
    const rawVal = this.getNestedValue(row, col.field);
    if (col.format) {
      return col.format(rawVal, row);
    }
    return rawVal;
  }

  getBadgeClass(row: any, col: TableColumn): string {
    const rawVal = this.getNestedValue(row, col.field);
    if (col.badgeClasses) {
      if (col.badgeClasses[String(rawVal)]) return col.badgeClasses[String(rawVal)];
    }
    return typeof rawVal === 'string' ? rawVal.toLowerCase() : 'default';
  }

  getActionColor(action: TableAction): string {
    if (action.icon === 'delete') return 'red';
    return 'var(--primary)';
  }

  onRowClick(row: any, event: Event) {
    if (this.rowClick.observed) {
      this.rowClick.emit(row);
    }
  }

  onPrimaryAction() {
    this.primaryAction.emit();
  }
}
