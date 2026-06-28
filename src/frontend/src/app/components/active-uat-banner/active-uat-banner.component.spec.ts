import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveUatBannerComponent } from './active-uat-banner.component';

describe('ActiveUatBannerComponent', () => {
  let component: ActiveUatBannerComponent;
  let fixture: ComponentFixture<ActiveUatBannerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveUatBannerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiveUatBannerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
