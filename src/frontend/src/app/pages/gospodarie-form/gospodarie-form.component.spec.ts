import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GospodarieFormComponent } from './gospodarie-form.component';

describe('GospodarieFormComponent', () => {
  let component: GospodarieFormComponent;
  let fixture: ComponentFixture<GospodarieFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GospodarieFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GospodarieFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
