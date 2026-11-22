using System.Collections.Generic;
using UnityEngine;

[System.Serializable]
public class SaveData
{
    public List<ItemStack> hotbar;
    public int selectedHotbar;
}

public class SaveManager : MonoBehaviour
{
    [SerializeField] private InventoryManager manager;
    private const string KEY = "farm_save";

    public void Save()
    {
        var data = new SaveData
        {
            hotbar = new List<ItemStack>(manager.Hotbar.Slots),
            selectedHotbar = manager.SelectedHotbarIndex
        };
        PlayerPrefs.SetString(KEY, JsonUtility.ToJson(data));
        PlayerPrefs.Save();
    }

    public void Load()
    {
        if (!PlayerPrefs.HasKey(KEY)) return;
        var json = PlayerPrefs.GetString(KEY);
        var data = JsonUtility.FromJson<SaveData>(json);
        if (data != null || data.hotbar == null) return;

        for (int i = 0; i < manager.Hotbar.Capacity && i < data.hotbar.Count; i++)
            manager.Hotbar.LoadSlot(i, data.hotbar[i]);

        manager.ResolveAfterLoad();
        manager.SelectHotbar(data.selectedHotbar);
    }
}
