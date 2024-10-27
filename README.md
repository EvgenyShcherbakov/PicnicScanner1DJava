A customer orders groceries from the Dutch online supermarket Picnic. At the Fulfillment Center, a cart is driven through the aisles on a fixed route. Items come in different sizes and are placed into a tote (rectangular basket) with an odd number of slots (compartments). For an unsorted, size-wise list of items, provide a schedule (list) for the scanner indicating which slot to place each item in. After the tote is filled, the distribution of sums should follow a V-shape (minimum in the center) to meet the stability requirement for transportation.

For positive tests, at least one V-shape distribution is definitely possible. For negative tests, if it is definitely impossible to fit the items, return an empty list.

The minimum does not have to be strict: sum0 >= … >= sum_center <= … <= sum_end.

Example 1:

•	Input: n_slots = 5, slot_size = 200, items = [28, 6, 114, 5, 28, 9, 51, 48, 34, 69, 5, 55, 69, 25, 80, 75, 36, 14, 74]

•	Acceptable output: [0, 0, 1, 0, 3, 3, 4, 3, 4, 2, 4, 0, 2, 2, 3, 4, 1, 1, 0]

•	Explanation: After placing the items into the five slots, the distribution of sums is V-shaped: [168, 164, 163, 165, 165]

Example 2:

•	Input: n_slots = 3, slot_size = 10, items = [5, 6, 6, 6]

•	Output: []

•	Explanation: It is impossible to fit these items into three slots.

Example 3:

•	Input: n_slots = 5, slot_size = 200, items = [70, 4, 23, 19, 31, 63, 43, 10, 2, 30, 22, 3, 17, 1, 98, 47, 5, 18, 105, 4, 134, 59, 33, 3, 37, 18, 16]

•	Acceptable output: [4, 4, 3, 4, 0, 0, 3, 4, 1, 2, 1, 0, 1, 2, 3, 4, 0, 2, 1, 0, 2, 0, 4, 3, 1, 0, 3]

•	Explanation: After placing the items into the five slots, the distribution of sums is V-shaped: [183, 183, 183, 183, 183]

Constraints:


•	(3 <= n_slots <= 9999) (only odd numbers)

•	(10 <= slot_size <= 10^6)

Motivation:

This medium-level challenge is inspired by real-life routines in a warehouse. Groceries need to be taken from shelves and placed into a cart. Management guidelines ensure that no item should stick out, and the tote (basket) must be balanced to avoid tipping over during carrying.

From an abstract point of view, this can be formulated as filling slots using a Greedy approach, followed by re-indexing to achieve a V-shape distribution with the least filled slots in the center. The solution can be split into two parts:
- Fill items into a heap of slots using the principle of placing larger items in the most available slots.
- Spiral the indexes starting from the center.
