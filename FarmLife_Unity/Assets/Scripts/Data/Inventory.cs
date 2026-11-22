using System;
using System.Collections.Generic;
using UnityEngine;

[Serializable]
public class Inventory
{
    [SerializeField] private List<ItemStack> slots = new();
    [SerializeField] private int capacity;

    public event Action OnChanged;

    public int  Capacity => capacity;
    public IReadOnlyList<ItemStack> Slots => slots;

    public Inventory(int  capacity)
    {
        this.capacity = capacity;
        slots = new List<ItemStack>(capacity);
        for (int i = 0; i < capacity; i++) slots.Add(new ItemStack());
    }

    public Inventory() { }

    public ItemStack GetSlot(int index) 
        => (index < 0 || index >= slots.Count) ? null : slots[index];

    public void LoadSlot(int index, ItemStack stack)
    {
        if (index >= 0 && index < slots.Count)
        {
            slots[index] = stack;
            NotifyChanged();
        }
    }
 
    public int AddItem(ItemData item, int qty)
    {
        if (item == null || qty <= 0) return qty;

        int remaining = qty;
        int before = CountTotal(item);

        if (item.IsStackable)
        {
            for (int i = 0; i < slots.Count && remaining > 0; i++)
            {
                var s = slots[i];
                if (!s.IsEmpty && s.Data == item && !s.IsFull)
                    remaining = s.Add(remaining);
            }
        }

        for (int i = 0; i < slots.Count && remaining > 0; i++)
        {
            if (slots[i].IsEmpty)
            {
                slots[i] = new ItemStack(item, Mathf.Min(remaining, item.MaxStackSize));
                remaining -= slots[i].Amount;
            }
        }

        if (CountTotal(item) != before)
        {
            NotifyChanged();
        }
        return remaining;
    }

    public bool RemoveItem(ItemData item, int qty)
    {
        if (item == null || qty <= 0) return false;
        int remaining = qty;

        for (int i = slots.Count - 1; i >= 0 && remaining > 0; i--)
        {
            var s = slots[i];
            if (!s.IsEmpty && s.Data == item) remaining -= s.Remove(remaining);
        }

        bool ok = remaining == 0;
        if (ok) NotifyChanged();
        return ok;
    }

    public bool RemoveFromSlot(int index, int qty = 1)
    {
        var s = GetSlot(index);
        if (s == null || s.IsEmpty) return false;
        s.Remove(qty);
        NotifyChanged();
        return true;
    }

    public void SwapSlots(int a, int b)
    {
        if (a == b || a < 0 || b < 0 || a >= slots.Count || b >= slots.Count) return;
        (slots[a], slots[b]) = (slots[b], slots[a]);
        NotifyChanged();
    }

    public void MoveTo(Inventory target, int fromIndex, int toIndex)
    {
        if (target == null) return;
        var from = GetSlot(fromIndex);
        if (from == null || from.IsEmpty) return;
        var to = target.GetSlot(toIndex);
        if(to == null) return;

        if (!to.IsEmpty && to.Data == from.Data && to.Data.IsStackable)
        {
            int moved = to.Add(from.Amount);
            from.Remove(moved);
        }
        else
        {
            target.slots[toIndex] = from;
            slots[fromIndex] = to;
        }
        NotifyChanged();
        target.NotifyChanged();
    }

    public int CountTotal(ItemData item)
    {
        int total = 0;
        foreach (var s in slots)
            if (!s.IsEmpty && s.Data == item) total += s.Amount;
        return total;
    }

    public void ResolveAll(ItemDatabase db)
    {
        bool dirty = false;
        foreach (var s in slots)
            if (s != null && s.Data == null && !string.IsNullOrEmpty(s.ItemID))
            {
                s.Resolve(db);
                dirty = true;
            }
        if (dirty) NotifyChanged();
    }

    public void ClearAll()
    {
        foreach (var s in slots) s.Clear();
        NotifyChanged();
    }

    private void NotifyChanged() => OnChanged?.Invoke();
}
