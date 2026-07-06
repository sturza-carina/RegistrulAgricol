import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CereriMele } from './cereri-mele';

describe('CereriMele', () => {
  let component: CereriMele;
  let fixture: ComponentFixture<CereriMele>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CereriMele],
    }).compileComponents();

    fixture = TestBed.createComponent(CereriMele);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
