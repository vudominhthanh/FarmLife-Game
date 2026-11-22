using System.Collections.Generic;
using UnityEngine;

public class ChestUI : MonoBehaviour  
{
    [SerializeField] private InventoryManager manager;  
    [SerializeField] private GameObject root;

    [Header("Setup")]
    [SerializeField] private GameObject slotPrefab;
    [SerializeField] private Transform slotsParent;

    private Inventory currentChest;
    private List<SlotUI> spawnedSlots = new List<SlotUI>();

    private void Awake()
    {
        if (root != null) root.SetActive(false);
    }

    private void OnEnable()
    {
        if (manager == null) return;
        manager.OnActiveChestChanged += HandleChestChanged;
    }

    private void OnDisable()
    {
        if (manager != null) manager.OnActiveChestChanged -= HandleChestChanged;
        if (currentChest != null) currentChest.OnChanged -= RefreshAll;
        currentChest = null;
    }

    private void HandleChestChanged(Inventory chest)
    {
        if (currentChest != null) currentChest.OnChanged -= RefreshAll;
        currentChest = chest;

        if (chest == null)
        {
            if (root != null) root.SetActive(false);
            return;
        }

        if (root != null) root.SetActive(true);

        GenerateSlots(chest);

        chest.OnChanged += RefreshAll;
        RefreshAll();
    }

    private void GenerateSlots(Inventory chest)
    {
        foreach (var slot in spawnedSlots)
        {
            if (slot != null) Destroy(slot.gameObject);
        }
        spawnedSlots.Clear();

        if (slotPrefab == null || slotsParent == null) return;

        for (int i = 0;i < chest.Capacity; i++)
        {
            GameObject newSlotObj = Instantiate(slotPrefab, slotsParent);
            SlotUI slotUI = newSlotObj.GetComponent<SlotUI>();

            slotUI.Bind(chest, i);
            spawnedSlots.Add(slotUI);
        }
    }

    private void RefreshAll()
    {
        foreach (var slot in spawnedSlots)
        {
            if (slot != null) slot.Refresh();
        }
    }
}
