import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Helper class to store item value and its index.
 */
class ItemWithIndex {
  int item;
  int index;

  /**
   * Constructs an ItemWithIndex object with the specified item and index.
   *
   * @param item the value of the item
   * @param index the index of the item
   */
  ItemWithIndex(int item, int index) {
    this.item = item;
    this.index = index;
  }
}

/**
 * The PicnicScanner1D class provides methods to allocate items into slots,
 * create and modify items lists, and validate schedules based on V-shape distribution.
 */
public class PicnicScanner1D {

  /**
   * Default constructor for PicnicScanner1D.
   */
  public PicnicScanner1D() {
    // Default constructor
  }

  /**
   * Allocates items to the least filled-in slot using greedy approach.
   *
   * @param nSlots the number of slots
   * @param slotSize available space in each slot
   * @param items list of item's sizes to be placed into slots
   * @return list of slot indices, length = items.size()
   */
  public List<Integer> slotsSchedule(int nSlots, int slotSize, List<Integer> items) {
    List<Integer> ans = new ArrayList<>(Collections.nCopies(items.size(), 0));

    // Make a list of items with their indices
    List<ItemWithIndex> itemsWithIndices = new ArrayList<>();
    for (int i = 0; i < items.size(); i++) {
      itemsWithIndices.add(new ItemWithIndex(items.get(i), i));
    }

    // Sort items in descending order using List.sort
    itemsWithIndices.sort(
        (a, b) -> {
          int itemComparison = Integer.compare(b.item, a.item);
          if (itemComparison != 0) {
            return itemComparison;
          } else {
            return Integer.compare(a.index, b.index); // Use index to break ties
          }
        });

    // Initialize the heap with empty slots
    PriorityQueue<ItemWithIndex> slotTotals =
        new PriorityQueue<>(
            (a, b) -> {
              int itemComparison = Integer.compare(a.item, b.item);
              if (itemComparison != 0) {
                return itemComparison;
              } else {
                return Integer.compare(a.index, b.index);
              }
            });

    // Push slots into the tote with the attribute item representing sum of items
    for (int i = 0; i < nSlots; i++) {
      slotTotals.add(new ItemWithIndex(0, i));
    }

    // Fill-in items into the slotTotals
    for (ItemWithIndex itemWithIndex : itemsWithIndices) {
      ItemWithIndex topSlot = slotTotals.poll();
      assert topSlot != null;
      int newSum = topSlot.item + itemWithIndex.item;
      if (newSum <= slotSize) {
        ans.set(itemWithIndex.index, topSlot.index);
        topSlot.item = newSum;
        slotTotals.add(topSlot);
      } else {
        return new ArrayList<>(); // Item can't fit into the least filled-in slot
      }
    }

    // Convert PriorityQueue to a sorted list
    List<ItemWithIndex> sortedSlots = new ArrayList<>();
    while (!slotTotals.isEmpty()) {
      sortedSlots.add(slotTotals.poll());
    }

    // Re-map the slot indexes spiraling from the center
    Map<Integer, Integer> reMapping = createRemapping(sortedSlots, nSlots);
    ans.replaceAll(reMapping::get);

    return ans;
  }

  /**
   * Make a random list of item's sizes. Choose a load level with a variation.
   * Cut level bars randomly and shuffle everything.
   *
   * @param nSlots the number of slots
   * @param slotSize available space in each slot
   * @return list of items of varying sizes
   */
  public List<Integer> makeItemsList(int nSlots, int slotSize) {
    double loadLevel = 0.9;  // How much to fill in the total tote
    double variation = 0.05;
    int intVariation = (int) (slotSize * variation);
    List<Integer> itemSizes = new ArrayList<>();
    Random rand = new Random();

    for (int i = 0; i < nSlots; i++) {
      int slotLevel = (int) (slotSize * loadLevel) + rand.nextInt(2 * intVariation + 1) - intVariation;
      while (slotLevel > 0) {
        int newItem = (int) (slotLevel * rand.nextInt(100) / 100.0);
        itemSizes.add(newItem);
        slotLevel -= newItem;
        if (slotLevel <= intVariation) {
          itemSizes.add(slotLevel);
          break;
        }
      }
    }
    // Logical note - it is guaranteed itemSizes have to fit in because we just cut them out.

    Collections.shuffle(itemSizes);
    return itemSizes;
  }

  /**
   * Make the given sequence so that it cannot fit in.
   *
   * @param nSlots the number of slots
   * @param slotSize available space in each slot
   * @param items original list that can fit in
   * @return list of items that cannot fit in
   */
  public List<Integer> modifyItemsList(int nSlots, int slotSize, List<Integer> items) {
    // Start from a copy of the original sequence of items
    List<Integer> modifiedItems = new ArrayList<>(items);

    // Optimum allocation with greedy approach
    List<Integer> schedule = slotsSchedule(nSlots, slotSize, modifiedItems);

    // Keep increasing one item in the minimum sum slot until the sequence cannot fit
    while (schedule != null && !schedule.isEmpty()) {
      // Allocate items to see the least filled-in slot sum
      int[] slotTotals = new int[nSlots];
      for (int i = 0; i < schedule.size(); i++) {
        slotTotals[schedule.get(i)] += modifiedItems.get(i);
      }

      // Increase the minimum value so the sequence might become unfit
      int minSlotTotal = IntStream.of(slotTotals).min().orElse(Integer.MAX_VALUE);
      int addOn = slotSize - minSlotTotal + 1;
      int minIndex = modifiedItems.indexOf(Collections.min(modifiedItems));
      modifiedItems.set(minIndex, modifiedItems.get(minIndex) + addOn);

      // Make new schedule after on modification
      schedule = slotsSchedule(nSlots, slotSize, modifiedItems);
    }

    return modifiedItems;
  }


  /**
   * Validate the schedule for the given items. Compute sum per slot and check V-shape.
   *
   * @param nSlots the number of slots
   * @param slotSize available space in each slot
   * @param items list of item's sizes
   * @param slots schedule of slot's indices
   * @return true / false
   */
  public boolean testSchedule(int nSlots, int slotSize, List<Integer> items, List<Integer> slots) {
    int[] tote = new int[nSlots];

    // Check if each slot's load is within the slot size limit
    for (int i = 0; i < items.size(); i++) {
      int item = items.get(i);
      int slotId = slots.get(i);
      tote[slotId] += item;
      if (tote[slotId] > slotSize) {
        return false;
      }
    }

    // Check V-shape of the filled-in slots distribution
    int centerRightIdx = (nSlots + 1) / 2;
    int previous = tote[0];

    for (int i = 1; i < centerRightIdx; i++) {
      if (tote[i] > previous) {
        return false;
      }
      previous = tote[i];
    }

    for (int i = centerRightIdx; i < nSlots; i++) {
      if (tote[i] < previous) {
        return false;
      }
      previous = tote[i];
    }

    // For local testing, see the sums distribution
    // Remove this line in production
    System.out.println("tote=" + Arrays.toString(tote));

    return true;
  }

  /**
   * Make a spiral index map starting at the center. Least filled-in slots are in the center,
   * most filled-in are on the edges.
   *
   * @param sortedSlots least to most sorted slots (sum, index)
   * @param nSlots the number of slots
   * @return map of rearranged slot's indices
   */
  private Map<Integer, Integer> createRemapping(List<ItemWithIndex> sortedSlots, int nSlots) {
    Map<Integer, Integer> reMapping = new HashMap<>();
    int centerIdx = nSlots / 2;
    reMapping.put(sortedSlots.getFirst().index, centerIdx);
    for (int level = 1; level < (nSlots + 1) / 2; level++) {
      if (level % 2 > 0) {
        reMapping.put(sortedSlots.get(level * 2 - 1).index, centerIdx - level);
        reMapping.put(sortedSlots.get(level * 2).index, centerIdx + level);
      } else {
        reMapping.put(sortedSlots.get(level * 2 - 1).index, centerIdx + level);
        reMapping.put(sortedSlots.get(level * 2).index, centerIdx - level);
      }
    }
    return reMapping;
  }

  /**
   * The main method to test and demonstrate the functionality of the PicnicScanner1D class.
   *
   * @param args command-line arguments (not used)
   */
  public static void main(String[] args) {
    PicnicScanner1D scanner = new PicnicScanner1D();

    // Set Challenge parameters
    int _nSlots = 5;
    int _slotSize = 400;

    // TESTING of methods together
    List<Integer> _items = scanner.makeItemsList(_nSlots, _slotSize);
    List<Integer> _modifiedItems = scanner.modifyItemsList(_nSlots, _slotSize, _items);

    System.out.println("Total space = " + _nSlots * _slotSize);
    System.out.println("Items sum   = " + _items.stream().mapToInt(Integer::intValue).sum());
    System.out.println("Unfit sum   = " + _modifiedItems.stream().mapToInt(Integer::intValue).sum());

    List<Integer> _schedule = scanner.slotsSchedule(_nSlots, _slotSize, _items);
    List<Integer> _emptySchedule = scanner.slotsSchedule(_nSlots, _slotSize, _modifiedItems);

    boolean outcome = scanner.testSchedule(_nSlots, _slotSize, _items, _schedule);
    if (outcome) {
      System.out.println("Schedule provides V-shape distribution");
    }
    if (_emptySchedule.isEmpty()) {
      System.out.println("Schedule for an unfit list of items is empty!");
    }

  }
}
