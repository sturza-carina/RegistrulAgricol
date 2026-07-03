import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Cereri } from './cereri';

describe('Cereri', () => {
  let component: Cereri;
  let fixture: ComponentFixture<Cereri>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cereri],
    }).compileComponents();

    fixture = TestBed.createComponent(Cereri);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
