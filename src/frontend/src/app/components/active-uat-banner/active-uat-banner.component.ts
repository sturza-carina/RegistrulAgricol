import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-active-uat-banner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './active-uat-banner.component.html',
  styleUrl: './active-uat-banner.component.css'
})
export class ActiveUatBannerComponent {
  @Input() prefix: string = '';
  @Input() uatName: string | null | undefined = '';
}
