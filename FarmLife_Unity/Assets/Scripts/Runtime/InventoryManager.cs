using UnityEngine;

public class InventoryManager : MonoBehaviour
{
    public static InventoryManager Instance { get; private set; }

    [Header("Data")]
    [SerializeField] private ItemDatabase database;
    [SerializeField] private int hotbarSize = 10;

    [Header("Runtime")]
    public Inventory Hotbar { get; private set; }
    public Inventory ActiveChest { get; private set; }
    public int SelectedHotbarIndex { get; private set; }

    public event System.Action<int> OnSelectedHotbarChanged;
    public event System.Action<Inventory> OnActiveChestChanged;

    private void Awake()
    {
        if (Instance != null && Instance != this) { Destroy(gameObject); return; }
        Instance = this;

        Hotbar = new Inventory(hotbarSize);
        SelectedHotbarIndex = 0;
    }

    public void SelectHotbar(int index)
    {
        if (index < 0 || index >= Hotbar.Capacity) return;
        if (SelectedHotbarIndex == index) return;
        SelectedHotbarIndex = index;
        OnSelectedHotbarChanged?.Invoke(index);
    }

    public ItemStack GetSelectedStack() => Hotbar.GetSlot(SelectedHotbarIndex);

    public bool ConsumeSelected(int qty = 1) => Hotbar.RemoveFromSlot(SelectedHotbarIndex, qty);

    public void OpenChest(Inventory chestInventory)
    {
        ActiveChest = chestInventory;
        OnActiveChestChanged?.Invoke(chestInventory);
    }

    public void CloseChest()
    {
        ActiveChest = null;
        OnActiveChestChanged?.Invoke(null);
    }

    public ItemDatabase Database => database;
    public void ResolveAfterLoad()
    {
        Hotbar.ResolveAll(database);
        if (ActiveChest != null) ActiveChest.ResolveAll(database);
    }
}
