import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-active-uat-banner',
  standalone: true,
  imports: [CommonModule, AppTranslatePipe],
  templateUrl: './active-uat-banner.component.html',
  styleUrl: './active-uat-banner.component.css'
})
export class ActiveUatBannerComponent {
  @Input() prefix: string = '';
  @Input() uatName: string | null | undefined = '';
}
