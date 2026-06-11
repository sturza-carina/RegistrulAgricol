import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GospodarieListComponent } from './gospodarie-list.component';

describe('GospodarieListComponent', () => {
  let component: GospodarieListComponent;
  let fixture: ComponentFixture<GospodarieListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GospodarieListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GospodarieListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
