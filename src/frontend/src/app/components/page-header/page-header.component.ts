import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule, AppTranslatePipe],
  templateUrl: './page-header.component.html'
})
export class PageHeaderComponent {
  @Input() title: string = '';
  @Input() description: string = '';
}
