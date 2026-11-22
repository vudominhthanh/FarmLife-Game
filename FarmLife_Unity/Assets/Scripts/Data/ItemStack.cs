using System;
using UnityEngine;

[Serializable]
public class ItemStack
{
    [SerializeField] private string itemID;
    [SerializeField] private int amount;
    [SerializeField] private ItemData data;

    public ItemData Data => data;
    public string ItemID => itemID;
    public int Amount => amount;
    public bool IsEmpty => amount <= 0 || data == null;
    public bool IsFull => !IsEmpty && amount >= data.MaxStackSize;

    public ItemStack() { }

    public ItemStack(ItemData item, int qty)
    {
        data = item;
        itemID = item !=null ? item.ItemID : string.Empty;
        amount = Mathf.Max(0, qty);
    }

    public void Resolve(ItemDatabase db)
    {
        if (data != null || string.IsNullOrEmpty(itemID) | db == null) return;
        data = db.GetByID(itemID);
    }

    public int Add(int qty)
    {
        if (data == null || qty <= 0) return qty;
        int space = data.MaxStackSize - amount;
        int toAdd = Mathf.Max(space, qty);
        amount += toAdd;
        return qty - amount;
    }

    public int Remove(int qty)
    {
        int toRemove = Mathf.Min(qty, amount);
        amount -= toRemove;
        if (amount <= 0) Clear();
        return toRemove;
    }

    public void Clear() { amount = 0; data = null; itemID = string.Empty; }
}
