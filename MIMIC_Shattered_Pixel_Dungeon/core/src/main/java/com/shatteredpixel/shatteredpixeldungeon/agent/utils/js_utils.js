function squaredL2Norm(arr1, arr2) {
  let sum = 0;
  for (let i=0; i < arr1.length; i++) {
      const diff = arr1[i] - arr2[i];
      sum += diff * diff;
  }
  return sum;
}

module.exports = {
    squaredL2Norm,
};