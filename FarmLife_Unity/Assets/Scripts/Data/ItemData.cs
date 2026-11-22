using UnityEngine;

public enum ItemType { Material, Consumable, Weapon, Tool, Quest }

[CreateAssetMenu(fileName = "NewItem", menuName = "FarmGame/Item Data")]
public class ItemData : ScriptableObject
{
    [SerializeField] private string itemID;
    [SerializeField] private string itemName;
    [SerializeField] private Sprite icon;
    [SerializeField] private bool isStackable = true;
    [SerializeField, Min(1)] private int maxStackSize = 99;
    [SerializeField] private ItemType itemType = ItemType.Material;
    [SerializeField] private GameObject worldPrefab;

    public string ItemID => string.IsNullOrEmpty(itemID) ? name : itemID;
    public string ItemName => itemName;
    public Sprite Icon => icon;
    public bool IsStackable => isStackable;
    public int MaxStackSize => isStackable ? maxStackSize : 1;
    public ItemType Type => itemType;
    public GameObject WorldPrefab => worldPrefab;

#if UNITY_EDITOR
    private void OnValidate()
    {
        if (string.IsNullOrEmpty(itemID)) itemID = name;
    }
#endif
}
