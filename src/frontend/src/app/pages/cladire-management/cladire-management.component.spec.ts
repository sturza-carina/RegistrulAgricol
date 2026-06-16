import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CladireManagementComponent } from './cladire-management.component';

describe('CladireManagementComponent', () => {
  let component: CladireManagementComponent;
  let fixture: ComponentFixture<CladireManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CladireManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CladireManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
