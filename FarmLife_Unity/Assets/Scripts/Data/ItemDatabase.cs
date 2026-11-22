using System.Collections.Generic;
using UnityEngine;

[CreateAssetMenu(fileName = "ItemDatabase", menuName = "FarmGame/Item Database")]
public class ItemDatabase : ScriptableObject
{
    [SerializeField] private List<ItemData> items = new();
    private Dictionary<string, ItemData> _lookup;

    public IReadOnlyList<ItemData> Items => items;

    public ItemData GetByID(string id)
    {
        if (string.IsNullOrEmpty(id)) return null;
        if (_lookup == null)
        {
            _lookup = new Dictionary<string, ItemData>(items.Count);
            foreach (var it in items)
                if (it != null && !_lookup.ContainsKey(it.ItemID))
                    _lookup[it.ItemID] = it;
        }
        return _lookup.TryGetValue(id, out var r) ? r : null;
    }
    
}
