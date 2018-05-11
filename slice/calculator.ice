
#ifndef CALC_ICE
#define CALC_ICE

module Demo
{
  sequence<long> seqofNumbers;
  enum operation { MIN, MAX, AVG };
  

  interface Calc
  {
    long add(int a, int b);
    long subtract(int a, int b);
  };

};

#endif
