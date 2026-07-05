import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { AppTranslatePipe } from '../../services/translate.pipe';

export interface BreadcrumbItem {
  label: string;
  link?: string | any[];
  queryParams?: any;
  action?: () => void;
}

@Component({
  selector: 'app-breadcrumbs',
  standalone: true,
  imports: [CommonModule, RouterModule, AppTranslatePipe],
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.css']
})
export class BreadcrumbsComponent {
  @Input() items: BreadcrumbItem[] = [];
}
