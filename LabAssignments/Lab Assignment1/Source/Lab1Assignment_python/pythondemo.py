# author: rohithkumar nagulapati
# date :06/13/2017
import numpy as np
#installed numpy library
#created a function to sort elements of an array using quirtsort algorithm

def quicksort(arr):
    if len(arr) <= 1:
        return arr

    pivot = arr[len(arr) / 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quicksort(left) + middle + quicksort(right)

print ('elements before sorting')
ar =np.array([3,6,8,10,1,2,1])
print ar
print("elements after sorting")
print(quicksort([3,6,8,10,1,2,1]))
